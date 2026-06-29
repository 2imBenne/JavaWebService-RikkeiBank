package com.rikkeibankproject.repository;

import com.rikkeibankproject.entity.Account;
import com.rikkeibankproject.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT t FROM Transaction t WHERE t.fromAccount = :account OR t.toAccount = :account ORDER BY t.timestamp DESC")
    List<Transaction> findByAccountOrderByTimestampDesc(@Param("account") Account account);
}
