package com.sovereign.domain.account.entity;

import com.sovereign.common.entity.BaseEntity;
import com.sovereign.common.enums.TransactionType;
import com.sovereign.domain.budget.entity.BudgetCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
@Getter
@Setter
public class Transaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_category_id")
    private BudgetCategory budgetCategory;

    @Column(name = "imported_transaction_id")
    private Long importedTransactionId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    private String description;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column(length = 64)
    private String fingerprint;
}