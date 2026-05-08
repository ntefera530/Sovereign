package com.sovereign.domain.networth.entity;

import com.sovereign.common.entity.BaseEntity;
import com.sovereign.common.enums.AssetType;
import com.sovereign.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "manual_assets")
@Getter
@Setter
public class ManualAsset extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType type;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal value;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private LocalDate valuationDate;

    private String notes;
}
