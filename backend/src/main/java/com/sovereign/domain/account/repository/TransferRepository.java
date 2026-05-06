package com.sovereign.domain.account.repository;

import com.sovereign.domain.account.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;  

public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    @Query("SELECT t FROM Transfer t WHERE " +
           "t.debitTransaction.account.id = :accountId OR " +
           "t.creditTransaction.account.id = :accountId")
    List<Transfer> findAllByAccountId(@Param("accountId") UUID accountId);

    List<Transfer> findByUserConfirmedFalseAndDismissedFalse();
}