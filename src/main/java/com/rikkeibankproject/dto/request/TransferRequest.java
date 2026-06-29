package com.rikkeibankproject.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotBlank(message = "Destination account is required")
    private String toAccountNumber;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000", message = "Minimum transfer amount is 1000")
    private BigDecimal amount;
    
    @NotBlank(message = "PIN code is required")
    private String pinCode;
    
    @NotBlank(message = "Description is required")
    private String description;
}
