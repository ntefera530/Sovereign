package com.sovereign.domain.budget.repository;

import com.sovereign.domain.budget.entity.Budget;
import com.sovereign.common.enums.BudgetPeriodType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    List<Budget> findByUserId(UUID userId);
    List<Budget> findByUserIdAndPeriodType(UUID userId, BudgetPeriodType periodType);
    List<Budget> findByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual( UUID userId, LocalDate date, LocalDate date2);
}