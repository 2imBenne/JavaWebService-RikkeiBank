package com.rikkeibankproject.service;

import com.rikkeibankproject.dto.request.TransferRequest;
import com.rikkeibankproject.entity.Account;
import com.rikkeibankproject.entity.User;
import com.rikkeibankproject.exception.CustomException;
import com.rikkeibankproject.repository.AccountRepository;
import com.rikkeibankproject.repository.TransactionRepository;
import com.rikkeibankproject.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CoreBankingServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CoreBankingService coreBankingService;

    private User mockUser;
    private Account mockSenderAccount;
    private Account mockReceiverAccount;

    @BeforeEach
    void setUp() {
        mockUser = User.builder().id(1L).username("testuser").build();
        mockSenderAccount = Account.builder()
                .id(1L)
                .accountNumber("12345")
                .balance(new BigDecimal("5000"))
                .pinCode("encodedPin")
                .user(mockUser)
                .build();
        
        mockReceiverAccount = Account.builder()
                .id(2L)
                .accountNumber("67890")
                .balance(new BigDecimal("1000"))
                .build();
    }

    @Test
    void getAccountBalance_Success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));
        when(accountRepository.findByUser(mockUser)).thenReturn(Optional.of(mockSenderAccount));

        Account result = coreBankingService.getAccountBalance("testuser");

        assertNotNull(result);
        assertEquals("12345", result.getAccountNumber());
        assertEquals(new BigDecimal("5000"), result.getBalance());
    }

    @Test
    void getAccountBalance_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> coreBankingService.getAccountBalance("unknown"));
    }

    @Test
    void transferMoney_Success() {
        TransferRequest request = new TransferRequest();
        request.setToAccountNumber("67890");
        request.setAmount(new BigDecimal("1000"));
        request.setPinCode("1234");
        request.setDescription("Test transfer");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(accountRepository.findByUser(mockUser)).thenReturn(Optional.of(mockSenderAccount));
        when(passwordEncoder.matches("1234", "encodedPin")).thenReturn(true);
        when(accountRepository.findByAccountNumberForUpdate("12345")).thenReturn(Optional.of(mockSenderAccount));
        when(accountRepository.findByAccountNumberForUpdate("67890")).thenReturn(Optional.of(mockReceiverAccount));

        coreBankingService.transferMoney("testuser", request);

        assertEquals(new BigDecimal("4000"), mockSenderAccount.getBalance());
        assertEquals(new BigDecimal("2000"), mockReceiverAccount.getBalance());
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    void transferMoney_InsufficientBalance_ThrowsException() {
        TransferRequest request = new TransferRequest();
        request.setToAccountNumber("67890");
        request.setAmount(new BigDecimal("10000")); // More than 5000
        request.setPinCode("1234");
        request.setDescription("Test transfer");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(accountRepository.findByUser(mockUser)).thenReturn(Optional.of(mockSenderAccount));
        when(passwordEncoder.matches("1234", "encodedPin")).thenReturn(true);
        when(accountRepository.findByAccountNumberForUpdate("12345")).thenReturn(Optional.of(mockSenderAccount));
        when(accountRepository.findByAccountNumberForUpdate("67890")).thenReturn(Optional.of(mockReceiverAccount));

        assertThrows(CustomException.class, () -> coreBankingService.transferMoney("testuser", request));
    }

    @Test
    void transferMoney_InvalidPin_ThrowsException() {
        TransferRequest request = new TransferRequest();
        request.setToAccountNumber("67890");
        request.setAmount(new BigDecimal("1000"));
        request.setPinCode("wrongPin");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        when(accountRepository.findByUser(mockUser)).thenReturn(Optional.of(mockSenderAccount));
        when(passwordEncoder.matches("wrongPin", "encodedPin")).thenReturn(false);

        assertThrows(CustomException.class, () -> coreBankingService.transferMoney("testuser", request));
    }
}
