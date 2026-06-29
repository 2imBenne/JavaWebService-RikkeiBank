package com.rikkeibankproject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rikkeibankproject.dto.request.TransferRequest;
import com.rikkeibankproject.entity.Account;
import com.rikkeibankproject.service.CoreBankingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CoreBankingController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for simple unit testing
public class CoreBankingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CoreBankingService coreBankingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "testuser")
    void getBalance_Success() throws Exception {
        Account mockAccount = new Account();
        mockAccount.setAccountNumber("12345");
        mockAccount.setBalance(new BigDecimal("5000"));

        when(coreBankingService.getAccountBalance(anyString())).thenReturn(mockAccount);

        mockMvc.perform(get("/api/v1/customer/banking/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountNumber").value("12345"))
                .andExpect(jsonPath("$.data.balance").value(5000));
    }

    @Test
    @WithMockUser(username = "testuser")
    void transferMoney_Success() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setToAccountNumber("67890");
        request.setAmount(new BigDecimal("1000"));
        request.setPinCode("1234");
        request.setDescription("Test transfer");

        doNothing().when(coreBankingService).transferMoney(anyString(), any(TransferRequest.class));

        mockMvc.perform(post("/api/v1/customer/banking/transfer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Money transferred successfully"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void transferMoney_ValidationFailed_AmountNull() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setToAccountNumber("67890");
        // amount is null
        request.setPinCode("1234");
        request.setDescription("Test transfer");

        mockMvc.perform(post("/api/v1/customer/banking/transfer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false)); // Should trigger GlobalExceptionHandler validation
    }

    @Test
    @WithMockUser(username = "testuser")
    void getStatement_Success() throws Exception {
        when(coreBankingService.getStatement(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/customer/banking/statement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(username = "testuser")
    void transferMoney_ValidationFailed_ToAccountBlank() throws Exception {
        TransferRequest request = new TransferRequest();
        request.setToAccountNumber(""); // Blank account
        request.setAmount(new BigDecimal("1000"));
        request.setPinCode("1234");
        request.setDescription("Test");

        mockMvc.perform(post("/api/v1/customer/banking/transfer")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
