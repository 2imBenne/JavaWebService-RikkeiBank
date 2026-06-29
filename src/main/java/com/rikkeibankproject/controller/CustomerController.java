package com.rikkeibankproject.controller;

import com.rikkeibankproject.dto.request.CustomerUpdateRequest;
import com.rikkeibankproject.dto.request.RegisterRequest;
import com.rikkeibankproject.dto.response.ApiResponse;
import com.rikkeibankproject.dto.response.CustomerProjectionDTO;
import com.rikkeibankproject.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/staff/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CustomerProjectionDTO>>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CustomerProjectionDTO> customers = customerService.getAllCustomers(pageable);
        return ResponseEntity.ok(ApiResponse.success(customers, "Customers retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createCustomer(@Valid @RequestBody RegisterRequest request) {
        customerService.createCustomer(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Customer created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateRequest request) {
        customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Customer updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Customer soft-deleted successfully"));
    }
}
