package com.sovereign.domain.networth.entity;

import com.sovereign.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "net_worth_asset_entries")
@Getter
@Setter
public class NetWorthAssetEntry extends BaseEntity {

    // no FK enforcement — historical record
    @Column(nullable = false)
    private UUID snapshotId;

    @Column(nullable = false)
    private String sourceType;

    @Column(nullable = false)
    private UUID sourceId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal value;
}