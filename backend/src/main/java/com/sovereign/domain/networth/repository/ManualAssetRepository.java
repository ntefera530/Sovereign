package com.sovereign.domain.networth.repository;

import com.sovereign.domain.networth.entity.ManualAsset;
import com.sovereign.common.enums.AssetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ManualAssetRepository extends JpaRepository<ManualAsset, UUID> {
    List<ManualAsset> findByUserId(UUID userId);
    List<ManualAsset> findByUserIdAndType(UUID userId, AssetType type);
}