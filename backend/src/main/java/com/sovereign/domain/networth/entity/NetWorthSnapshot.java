package com.sovereign.domain.networth.entity;

import com.sovereign.common.entity.BaseEntity;
import com.sovereign.common.enums.NetWorthCalculationType;
import com.sovereign.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "net_worth_snapshots")
@Getter
@Setter
public class NetWorthSnapshot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NetWorthCalculationType calculationType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAssets;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal totalLiabilities;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal netWorth;

    @Column(nullable = false)
    private LocalDateTime snapshotDate;
}