package com.rikkeibankproject.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class CustomerUpdateRequest {
    @Email(message = "Email must be valid")
    private String email;
    
    private Boolean isActive;
}
