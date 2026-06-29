package com.rikkeibankproject.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class KycSubmitRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;
    
    private String address;
    
    @NotBlank(message = "Identity card number is required")
    private String identityCard;
}
