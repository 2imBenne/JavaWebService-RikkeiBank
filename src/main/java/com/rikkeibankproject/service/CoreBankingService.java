package com.rikkeibankproject.service;

import com.rikkeibankproject.dto.request.ChangePinRequest;
import com.rikkeibankproject.dto.request.TransferRequest;
import com.rikkeibankproject.dto.response.TransactionResponse;
import com.rikkeibankproject.entity.Account;
import com.rikkeibankproject.entity.Transaction;
import com.rikkeibankproject.entity.User;
import com.rikkeibankproject.exception.CustomException;
import com.rikkeibankproject.repository.AccountRepository;
import com.rikkeibankproject.repository.TransactionRepository;
import com.rikkeibankproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoreBankingService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Account getAccountBalance(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
        return accountRepository.findByUser(user)
                .orElseThrow(() -> new CustomException("Account not found for user", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void transferMoney(String username, TransferRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));
                
        Account senderAccount = accountRepository.findByUser(user)
                .orElseThrow(() -> new CustomException("Sender account not found", HttpStatus.NOT_FOUND));

        if (senderAccount.getAccountNumber().equals(request.getToAccountNumber())) {
            throw new CustomException("Cannot transfer money to your own account", HttpStatus.BAD_REQUEST);
        }

        // Verify PIN
        if (!passwordEncoder.matches(request.getPinCode(), senderAccount.getPinCode())) {
            throw new CustomException("Invalid PIN code", HttpStatus.FORBIDDEN);
        }

        // Apply Pessimistic Lock to avoid double-spending
        Account lockedSender = accountRepository.findByAccountNumberForUpdate(senderAccount.getAccountNumber())
                .orElseThrow(() -> new CustomException("Sender account not found", HttpStatus.NOT_FOUND));
                
        Account lockedReceiver = accountRepository.findByAccountNumberForUpdate(request.getToAccountNumber())
                .orElseThrow(() -> new CustomException("Receiver account not found", HttpStatus.NOT_FOUND));

        // Check balance
        if (lockedSender.getBalance().compareTo(request.getAmount()) < 0) {
            throw new CustomException("Insufficient balance", HttpStatus.CONFLICT);
        }

        // Execute transfer
        lockedSender.setBalance(lockedSender.getBalance().subtract(request.getAmount()));
        lockedReceiver.setBalance(lockedReceiver.getBalance().add(request.getAmount()));
        
        accountRepository.save(lockedSender);
        accountRepository.save(lockedReceiver);

        // Save transaction record
        Transaction transaction = Transaction.builder()
                .type(Transaction.TransactionType.TRANSFER)
                .amount(request.getAmount())
                .fromAccount(lockedSender)
                .toAccount(lockedReceiver)
                .description(request.getDescription())
                .timestamp(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);
    }

    public List<TransactionResponse> getStatement(String username) {
        Account account = getAccountBalance(username); // Re-use to get user's account
        
        List<Transaction> transactions = transactionRepository.findByAccountOrderByTimestampDesc(account);
        
        return transactions.stream().map(t -> {
            String flowType = t.getFromAccount().getId().equals(account.getId()) ? "DEBIT" : "CREDIT";
            
            return TransactionResponse.builder()
                    .id(t.getId())
                    .type(t.getType().name())
                    .amount(t.getAmount())
                    .fromAccount(t.getFromAccount().getAccountNumber())
                    .toAccount(t.getToAccount() != null ? t.getToAccount().getAccountNumber() : null)
                    .description(t.getDescription())
                    .timestamp(t.getTimestamp())
                    .flowType(flowType)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional
    public void changePin(String username, ChangePinRequest request) {
        Account account = getAccountBalance(username);
        
        if (!passwordEncoder.matches(request.getOldPin(), account.getPinCode())) {
            throw new CustomException("Invalid old PIN code", HttpStatus.FORBIDDEN);
        }
        
        account.setPinCode(passwordEncoder.encode(request.getNewPin()));
        accountRepository.save(account);
    }

    @Transactional
    public String forgotPin(String username, com.rikkeibankproject.dto.request.ForgotPinRequest request) {
        Account account = getAccountBalance(username);
        User user = account.getUser();

        if (user.getKycProfile() == null || !user.getKycProfile().getIdentityCard().equals(request.getIdentityCard())) {
            throw new CustomException("Identity card does not match or KYC not completed", HttpStatus.BAD_REQUEST);
        }

        // Generate new 6-digit PIN
        String newPin = org.apache.commons.lang3.RandomStringUtils.randomNumeric(6);
        account.setPinCode(passwordEncoder.encode(newPin));
        accountRepository.save(account);

        return newPin;
    }
}
