package com.sovereign.domain.debt.entity;

import com.sovereign.common.entity.BaseEntity;
import com.sovereign.common.enums.DebtType;
import com.sovereign.common.enums.PaymentFrequency;
import com.sovereign.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "debts")
@Getter
@Setter
public class Debt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal principal;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal currentBalance;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal interestRate;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal minimumPayment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentFrequency paymentFrequency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DebtType debtType;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private boolean isPaidOff = false;
}