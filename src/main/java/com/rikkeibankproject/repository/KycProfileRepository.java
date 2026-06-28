package com.rikkeibankproject.repository;

import com.rikkeibankproject.entity.KycProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KycProfileRepository extends JpaRepository<KycProfile, Long> {
}
