package com.rikkeibankproject.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePinRequest {
    @NotBlank(message = "Old PIN is required")
    private String oldPin;
    
    @NotBlank(message = "New PIN is required")
    @Size(min = 4, max = 6, message = "New PIN must be 4 to 6 digits")
    private String newPin;
}
