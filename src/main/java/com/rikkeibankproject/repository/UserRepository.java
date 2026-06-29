package com.rikkeibankproject.repository;

import com.rikkeibankproject.dto.response.CustomerProjectionDTO;
import com.rikkeibankproject.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT new com.rikkeibankproject.dto.response.CustomerProjectionDTO(u.id, u.username, u.email, u.isKyc) FROM User u JOIN u.role r WHERE r.name = 'CUSTOMER'")
    Page<CustomerProjectionDTO> findAllCustomers(Pageable pageable);
}
