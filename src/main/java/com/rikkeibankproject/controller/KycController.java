package com.rikkeibankproject.controller;

import com.rikkeibankproject.dto.request.KycSubmitRequest;
import com.rikkeibankproject.dto.response.ApiResponse;
import com.rikkeibankproject.entity.KycProfile;
import com.rikkeibankproject.service.KycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    @PostMapping("/customer/kyc/submit")
    public ResponseEntity<ApiResponse<KycProfile>> submitKyc(
            Authentication authentication,
            @Valid @ModelAttribute KycSubmitRequest request,
            @RequestParam("frontCard") MultipartFile frontCard,
            @RequestParam("backCard") MultipartFile backCard
    ) throws IOException {
        String username = authentication.getName();
        KycProfile savedProfile = kycService.submitKyc(username, request, frontCard, backCard);
        return ResponseEntity.ok(ApiResponse.success(savedProfile, "KYC Profile submitted successfully"));
    }

    @PostMapping("/admin/kyc/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveKyc(@PathVariable Long id) {
        kycService.approveKyc(id);
        return ResponseEntity.ok(ApiResponse.success(null, "KYC Profile approved successfully"));
    }

    @PostMapping("/admin/kyc/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectKyc(@PathVariable Long id) {
        kycService.rejectKyc(id);
        return ResponseEntity.ok(ApiResponse.success(null, "KYC Profile rejected successfully"));
    }
}
