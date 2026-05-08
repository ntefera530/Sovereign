package com.sovereign.domain.networth.repository;

import com.sovereign.domain.networth.entity.NetWorthSnapshot;
import com.sovereign.common.enums.NetWorthCalculationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NetWorthSnapshotRepository extends JpaRepository<NetWorthSnapshot, UUID> {
    List<NetWorthSnapshot> findByUserIdOrderBySnapshotDateDesc(UUID userId);
    List<NetWorthSnapshot> findByUserIdAndCalculationTypeOrderBySnapshotDateDesc(
        UUID userId, NetWorthCalculationType calculationType);
}