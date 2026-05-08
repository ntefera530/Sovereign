package com.sovereign.domain.networth.repository;

import com.sovereign.domain.networth.entity.NetWorthAssetEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NetWorthAssetEntryRepository extends JpaRepository<NetWorthAssetEntry, UUID> {
    List<NetWorthAssetEntry> findBySnapshotId(UUID snapshotId);
}