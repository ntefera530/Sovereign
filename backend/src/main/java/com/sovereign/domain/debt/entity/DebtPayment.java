package com.sovereign.domain.debt.entity;

import com.sovereign.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "debt_payments")
@Getter
@Setter
public class DebtPayment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "debt_id", nullable = false)
    private Debt debt;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal extraAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate paymentDate;

    private String note;
}