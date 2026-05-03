package com.sovereign.domain.budget.repository;

import com.sovereign.domain.budget.entity.BudgetCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface BudgetCategoryRepository extends JpaRepository<BudgetCategory, UUID> {
    List<BudgetCategory> findByBudgetId(UUID budgetId);

    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.budget.user.id = :userId")
    List<BudgetCategory> findAllByUserId(@Param("userId") UUID userId);
}