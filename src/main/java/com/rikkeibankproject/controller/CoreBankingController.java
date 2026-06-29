package com.rikkeibankproject.controller;

import com.rikkeibankproject.dto.request.ChangePinRequest;
import com.rikkeibankproject.dto.request.TransferRequest;
import com.rikkeibankproject.dto.response.ApiResponse;
import com.rikkeibankproject.dto.response.TransactionResponse;
import com.rikkeibankproject.entity.Account;
import com.rikkeibankproject.service.CoreBankingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/customer/banking")
@RequiredArgsConstructor
public class CoreBankingController {

    private final CoreBankingService coreBankingService;

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBalance(Authentication authentication) {
        Account account = coreBankingService.getAccountBalance(authentication.getName());
        Map<String, Object> data = new HashMap<>();
        data.put("accountNumber", account.getAccountNumber());
        data.put("balance", account.getBalance());
        return ResponseEntity.ok(ApiResponse.success(data, "Balance retrieved successfully"));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<Void>> transferMoney(Authentication authentication, @Valid @RequestBody TransferRequest request) {
        coreBankingService.transferMoney(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Money transferred successfully"));
    }

    @GetMapping("/statement")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getStatement(Authentication authentication) {
        List<TransactionResponse> statements = coreBankingService.getStatement(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(statements, "Statement retrieved successfully"));
    }

    @PostMapping("/change-pin")
    public ResponseEntity<ApiResponse<Void>> changePin(
            @Valid @RequestBody ChangePinRequest request,
            Authentication authentication) {
        coreBankingService.changePin(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "PIN code changed successfully"));
    }

    @PostMapping("/forgot-pin")
    public ResponseEntity<ApiResponse<String>> forgotPin(
            @Valid @RequestBody com.rikkeibankproject.dto.request.ForgotPinRequest request,
            Authentication authentication) {
        String newPin = coreBankingService.forgotPin(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(newPin, "PIN reset successfully. Your new PIN is in the data field."));
    }
}
