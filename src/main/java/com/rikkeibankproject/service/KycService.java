package com.rikkeibankproject.service;

import com.rikkeibankproject.dto.request.KycSubmitRequest;
import com.rikkeibankproject.entity.KycProfile;
import com.rikkeibankproject.entity.KycStatus;
import com.rikkeibankproject.entity.User;
import com.rikkeibankproject.exception.CustomException;
import com.rikkeibankproject.repository.KycProfileRepository;
import com.rikkeibankproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class KycService {

    private final KycProfileRepository kycProfileRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public KycProfile submitKyc(String username, KycSubmitRequest request, MultipartFile frontCard, MultipartFile backCard) throws IOException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        if (user.isKyc()) {
            throw new CustomException("User already has a confirmed KYC profile", HttpStatus.BAD_REQUEST);
        }

        if (kycProfileRepository.existsByUser(user)) {
            KycProfile existingProfile = kycProfileRepository.findByUser(user).get();
            if (existingProfile.getStatus() == KycStatus.PENDING) {
                throw new CustomException("A KYC profile is already pending review", HttpStatus.BAD_REQUEST);
            }
        }

        String frontCardUrl = cloudinaryService.uploadFile(frontCard);
        String backCardUrl = cloudinaryService.uploadFile(backCard);

        KycProfile kycProfile = KycProfile.builder()
                .user(user)
                .fullName(request.getFullName())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .identityCard(request.getIdentityCard())
                .frontCardUrl(frontCardUrl)
                .backCardUrl(backCardUrl)
                .status(KycStatus.PENDING)
                .build();

        return kycProfileRepository.save(kycProfile);
    }

    @Transactional
    public void approveKyc(Long kycProfileId) {
        KycProfile profile = kycProfileRepository.findById(kycProfileId)
                .orElseThrow(() -> new CustomException("KYC Profile not found", HttpStatus.NOT_FOUND));
        
        profile.setStatus(KycStatus.CONFIRMED);
        kycProfileRepository.save(profile);
        
        User user = profile.getUser();
        user.setKyc(true);
        userRepository.save(user);
    }

    @Transactional
    public void rejectKyc(Long kycProfileId) {
        KycProfile profile = kycProfileRepository.findById(kycProfileId)
                .orElseThrow(() -> new CustomException("KYC Profile not found", HttpStatus.NOT_FOUND));
        
        profile.setStatus(KycStatus.REJECTED);
        kycProfileRepository.save(profile);
        
        User user = profile.getUser();
        user.setKyc(false);
        userRepository.save(user);
    }
}
