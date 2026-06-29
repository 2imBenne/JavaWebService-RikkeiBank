package com.rikkeibankproject.service;

import com.rikkeibankproject.dto.response.CustomerProjectionDTO;
import com.rikkeibankproject.dto.request.CustomerUpdateRequest;
import com.rikkeibankproject.dto.request.RegisterRequest;
import com.rikkeibankproject.entity.Account;
import com.rikkeibankproject.entity.Role;
import com.rikkeibankproject.entity.User;
import com.rikkeibankproject.exception.CustomException;
import com.rikkeibankproject.repository.AccountRepository;
import com.rikkeibankproject.repository.RoleRepository;
import com.rikkeibankproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<CustomerProjectionDTO> getAllCustomers(Pageable pageable) {
        return userRepository.findAllCustomers(pageable);
    }

    @Transactional
    public void createCustomer(RegisterRequest request) {
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

        Account account = Account.builder()
                .accountNumber(RandomStringUtils.randomNumeric(10))
                .balance(BigDecimal.ZERO)
                .user(user)
                .pinCode(passwordEncoder.encode("123456"))
                .build();
        accountRepository.save(account);
    }

    @Transactional
    public void updateCustomer(Long id, CustomerUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                throw new CustomException("Email Address already in use!", HttpStatus.BAD_REQUEST);
            }
            user.setEmail(request.getEmail());
        }

        if (request.getIsActive() != null) {
            user.setActive(request.getIsActive());
        }

        userRepository.save(user);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
        
        user.setActive(false);
        userRepository.save(user);
    }
}
