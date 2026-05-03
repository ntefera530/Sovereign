package com.sovereign.domain.budget.entity;

import com.sovereign.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "budget_categories")
@Getter
@Setter
public class BudgetCategory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal limitAmount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal spentAmount = BigDecimal.ZERO;
}