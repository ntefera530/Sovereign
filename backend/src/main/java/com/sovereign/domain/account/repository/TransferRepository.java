package com.sovereign.domain.account.repository;

import com.sovereign.domain.account.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;  

public interface TransferRepository extends JpaRepository<Transfer, UUID> {
    List<Transfer> findByFromAccountId(UUID accountId);
    List<Transfer> findByToAccountId(UUID accountId);
}