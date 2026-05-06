package com.sovereign.domain.account.repository;

import com.sovereign.domain.account.entity.Transaction;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByAccountId(UUID accountId);
    List<Transaction> findByAccountIdOrderByTransactionDateDesc(UUID accountId);
    List<Transaction> findByAccountIdAndTransactionDateBetween(UUID accountId, LocalDate start, LocalDate end);
    Optional<Transaction> findByFingerprint(String fingerprint);
    boolean existsByFingerprint(String fingerprint);

    @Modifying
    @Transactional
    void deleteByAccountId(UUID accountId);
}