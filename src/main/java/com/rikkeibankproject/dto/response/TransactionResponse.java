package com.rikkeibankproject.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private Long id;
    private String type;
    private BigDecimal amount;
    private String fromAccount;
    private String toAccount;
    private String description;
    private LocalDateTime timestamp;
    private String flowType; // DEBIT or CREDIT
}
