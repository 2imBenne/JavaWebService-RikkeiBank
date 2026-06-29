package com.rikkeibankproject.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPinRequest {
    @NotBlank(message = "Identity card number is required")
    private String identityCard;
}
