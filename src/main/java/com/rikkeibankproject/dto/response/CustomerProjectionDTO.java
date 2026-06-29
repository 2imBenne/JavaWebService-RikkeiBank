package com.rikkeibankproject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerProjectionDTO {
    private Long id;
    private String username;
    private String email;
    private boolean isKyc;
}
