package com.sovereign.domain.plaid.repository;

import com.sovereign.domain.plaid.entity.PlaidItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaidItemRepository extends JpaRepository<PlaidItem, UUID> {
    List<PlaidItem> findByUserId(UUID userId);
    Optional<PlaidItem> findByItemId(String itemId);
}