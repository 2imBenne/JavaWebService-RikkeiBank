package com.rikkeibankproject.repository;

import com.rikkeibankproject.entity.TokenBlackList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenBlackListRepository extends JpaRepository<TokenBlackList, Long> {
    boolean existsByToken(String token);
}
