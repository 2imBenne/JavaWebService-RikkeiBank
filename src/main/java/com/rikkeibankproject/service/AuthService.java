package com.rikkeibankproject.service;

import com.rikkeibankproject.dto.request.LoginRequest;
import com.rikkeibankproject.dto.request.RefreshTokenRequest;
import com.rikkeibankproject.dto.response.TokenResponse;
import com.rikkeibankproject.entity.RefreshToken;
import com.rikkeibankproject.entity.TokenBlackList;
import com.rikkeibankproject.entity.User;
import com.rikkeibankproject.exception.CustomException;
import com.rikkeibankproject.repository.RefreshTokenRepository;
import com.rikkeibankproject.repository.TokenBlackListRepository;
import com.rikkeibankproject.repository.UserRepository;
import com.rikkeibankproject.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;

import com.rikkeibankproject.dto.request.RegisterRequest;
import com.rikkeibankproject.entity.Account;
import com.rikkeibankproject.entity.Role;
import com.rikkeibankproject.repository.AccountRepository;
import com.rikkeibankproject.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.apache.commons.lang3.RandomStringUtils;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final TokenBlackListRepository tokenBlackListRepository;
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshExpirationDateInMs;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException("Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("Email Address already in use!", HttpStatus.BAD_REQUEST);
        }

        Role userRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new CustomException("User Role not set.", HttpStatus.INTERNAL_SERVER_ERROR));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .isKyc(false)
                .isActive(true)
                .build();

        user = userRepository.save(user);

        // Auto create an account for the new user
        Account account = Account.builder()
                .accountNumber(RandomStringUtils.randomNumeric(10))
                .balance(BigDecimal.ZERO)
                .user(user)
                .pinCode(passwordEncoder.encode("123456"))
                .build();
        accountRepository.save(account);
    }

    @Transactional
    public TokenResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateAccessToken(authentication);
        String refreshTokenString = tokenProvider.generateRefreshToken(loginRequest.getUsername());

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        // Delete old refresh tokens
        refreshTokenRepository.deleteByUser(user);

        // Save new refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .user(user)
                .expiryDate(LocalDateTime.now().plusNanos(refreshExpirationDateInMs * 1_000_000))
                .build();
        refreshTokenRepository.save(refreshToken);

        return new TokenResponse(accessToken, refreshTokenString);
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new CustomException("Refresh token is not in database!", HttpStatus.FORBIDDEN));

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new CustomException("Refresh token was expired. Please make a new signin request", HttpStatus.FORBIDDEN);
        }

        if (!tokenProvider.validateToken(requestRefreshToken)) {
            throw new CustomException("Invalid refresh token", HttpStatus.FORBIDDEN);
        }

        String username = tokenProvider.getUsernameFromJWT(requestRefreshToken);
        User user = refreshToken.getUser();

        if (!user.getUsername().equals(username)) {
            throw new CustomException("Token does not belong to user", HttpStatus.FORBIDDEN);
        }

        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), null, null); // Simplified for token generation
        
        // Use custom user details mechanism to generate valid token
        org.springframework.security.core.userdetails.UserDetails userDetails = new com.rikkeibankproject.security.CustomUserDetails(user);
        authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String newAccessToken = tokenProvider.generateAccessToken(authentication);

        return new TokenResponse(newAccessToken, requestRefreshToken);
    }

    @Transactional
    public void logout(String authorizationHeader) {
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            if (tokenProvider.validateToken(token)) {
                String username = tokenProvider.getUsernameFromJWT(token);
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

                refreshTokenRepository.deleteByUser(user);

                TokenBlackList tokenBlackList = TokenBlackList.builder()
                        .token(token)
                        .blacklistedAt(LocalDateTime.now())
                        .build();
                tokenBlackListRepository.save(tokenBlackList);
            }
        }
    }

    @Transactional
    public String forgotPassword(com.rikkeibankproject.dto.request.ForgotPasswordRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
                
        if (!user.getEmail().equals(request.getEmail())) {
            throw new CustomException("Email does not match", HttpStatus.BAD_REQUEST);
        }

        if (user.getKycProfile() == null || !user.getKycProfile().getIdentityCard().equals(request.getIdentityCard())) {
            throw new CustomException("Identity card does not match or KYC not completed", HttpStatus.BAD_REQUEST);
        }
        
        // Generate new 8-character password
        String newPassword = RandomStringUtils.randomAlphanumeric(8);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return newPassword;
    }
}
