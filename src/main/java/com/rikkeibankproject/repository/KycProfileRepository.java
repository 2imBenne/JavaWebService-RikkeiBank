package com.rikkeibankproject.repository;

import com.rikkeibankproject.entity.KycProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import com.rikkeibankproject.entity.User;
import java.util.Optional;

public interface KycProfileRepository extends JpaRepository<KycProfile, Long> {
    boolean existsByUser(User user);
    Optional<KycProfile> findByUser(User user);
}
