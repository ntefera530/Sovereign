package com.sovereign.domain.user.repository;

import com.sovereign.domain.user.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(UUID userId);
}