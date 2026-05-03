package com.sovereign.domain.account.repository;

import com.sovereign.domain.account.entity.Account;
import com.sovereign.common.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByUserId(UUID userId);
    List<Account> findByUserIdAndIsActiveTrue(UUID userId);
    List<Account> findByUserIdAndType(UUID userId, AccountType type);
}