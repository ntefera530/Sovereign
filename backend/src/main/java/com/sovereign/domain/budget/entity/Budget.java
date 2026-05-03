package com.sovereign.domain.budget.entity;

import com.sovereign.common.entity.BaseEntity;
import com.sovereign.common.enums.BudgetPeriodType;
import com.sovereign.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "budgets")
@Getter
@Setter
public class Budget extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BudgetPeriodType periodType;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;
}