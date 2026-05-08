package com.sovereign.domain.budget.service;

import com.sovereign.config.security.UserDetailsImpl;
import com.sovereign.domain.budget.dto.request.*;
import com.sovereign.domain.budget.dto.response.*;
import com.sovereign.domain.budget.entity.Budget;
import com.sovereign.domain.budget.entity.BudgetCategory;
import com.sovereign.domain.budget.repository.BudgetCategoryRepository;
import com.sovereign.domain.budget.repository.BudgetRepository;
import com.sovereign.exception.exceptions.BadRequestException;
import com.sovereign.exception.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;

    // ── Budget CRUD ───────────────────────────────────────────────

    @Transactional
    public BudgetResponse createBudget(UserDetailsImpl userDetails,
                                        CreateBudgetRequest request) {
        if (request.endDate() != null &&
                request.endDate().isBefore(request.startDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }

        Budget budget = new Budget();
        budget.setUser(userDetails.getUser());
        budget.setName(request.name());
        budget.setPeriodType(request.periodType());
        budget.setStartDate(request.startDate());
        budget.setEndDate(request.endDate());
        budgetRepository.save(budget);

        log.info("Budget created: {} for user: {}",
            budget.getId(), userDetails.getUser().getId());
        return toBudgetResponse(budget);
    }

    public List<BudgetResponse> getAllBudgets(UserDetailsImpl userDetails) {
        return budgetRepository
                .findByUserId(userDetails.getUser().getId())
                .stream()
                .map(this::toBudgetResponse)
                .toList();
    }

    public BudgetResponse getBudget(UserDetailsImpl userDetails, UUID budgetId) {
        return toBudgetResponse(findBudgetForUser(budgetId,
            userDetails.getUser().getId()));
    }

    @Transactional
    public BudgetResponse updateBudget(UserDetailsImpl userDetails,
                                        UUID budgetId, UpdateBudgetRequest request) {
        Budget budget = findBudgetForUser(budgetId, userDetails.getUser().getId());

        if (request.endDate() != null &&
                request.endDate().isBefore(budget.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }

        budget.setName(request.name());
        budget.setEndDate(request.endDate());
        budgetRepository.save(budget);

        log.info("Budget updated: {}", budgetId);
        return toBudgetResponse(budget);
    }

    @Transactional
    public void deleteBudget(UserDetailsImpl userDetails, UUID budgetId) {
        Budget budget = findBudgetForUser(budgetId, userDetails.getUser().getId());
        budgetCategoryRepository.deleteByBudgetId(budgetId);
        budgetRepository.delete(budget);
        log.info("Budget deleted: {}", budgetId);
    }

    // ── Categories ────────────────────────────────────────────────

    @Transactional
    public BudgetCategoryResponse createCategory(UserDetailsImpl userDetails,
                                                  UUID budgetId,
                                                  CreateCategoryRequest request) {
        Budget budget = findBudgetForUser(budgetId, userDetails.getUser().getId());

        BudgetCategory category = new BudgetCategory();
        category.setBudget(budget);
        category.setName(request.name());
        category.setLimitAmount(request.limitAmount());
        category.setSpentAmount(BigDecimal.ZERO);
        budgetCategoryRepository.save(category);

        log.info("Category created: {} in budget: {}", category.getId(), budgetId);
        return toCategoryResponse(category);
    }

    public List<BudgetCategoryResponse> getCategories(UserDetailsImpl userDetails,
                                                        UUID budgetId) {
        findBudgetForUser(budgetId, userDetails.getUser().getId());
        return budgetCategoryRepository.findByBudgetId(budgetId)
                .stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    @Transactional
    public BudgetCategoryResponse updateCategory(UserDetailsImpl userDetails,
                                                  UUID budgetId, UUID categoryId,
                                                  UpdateCategoryRequest request) {
        findBudgetForUser(budgetId, userDetails.getUser().getId());
        BudgetCategory category = budgetCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        category.setName(request.name());
        category.setLimitAmount(request.limitAmount());
        budgetCategoryRepository.save(category);

        log.info("Category updated: {}", categoryId);
        return toCategoryResponse(category);
    }

    @Transactional
    public void deleteCategory(UserDetailsImpl userDetails,
                                UUID budgetId, UUID categoryId) {
        findBudgetForUser(budgetId, userDetails.getUser().getId());
        BudgetCategory category = budgetCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        budgetCategoryRepository.delete(category);
        log.info("Category deleted: {}", categoryId);
    }

    // ── Summary ───────────────────────────────────────────────────

    public BudgetSummaryResponse getSummary(UserDetailsImpl userDetails,
                                             UUID budgetId) {
        findBudgetForUser(budgetId, userDetails.getUser().getId());
        List<BudgetCategory> categories = budgetCategoryRepository
                .findByBudgetId(budgetId);

        BigDecimal totalLimit = categories.stream()
                .map(BudgetCategory::getLimitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSpent = categories.stream()
                .map(BudgetCategory::getSpentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRemaining = totalLimit.subtract(totalSpent);

        BigDecimal percentageUsed = totalLimit.compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : totalSpent.divide(totalLimit, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);

        int categoriesOverBudget = (int) categories.stream()
                .filter(c -> c.getSpentAmount()
                    .compareTo(c.getLimitAmount()) > 0)
                .count();

        int categoriesApproachingLimit = (int) categories.stream()
                .filter(c -> {
                    if (c.getLimitAmount().compareTo(BigDecimal.ZERO) == 0)
                        return false;
                    BigDecimal pct = c.getSpentAmount()
                            .divide(c.getLimitAmount(), 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"));
                    return pct.compareTo(new BigDecimal("80")) >= 0
                        && pct.compareTo(new BigDecimal("100")) < 0;
                })
                .count();

        String insight = buildInsight(totalSpent, totalLimit,
            categoriesOverBudget, categoriesApproachingLimit, categories);

        return new BudgetSummaryResponse(
            totalLimit.setScale(2, RoundingMode.HALF_UP),
            totalSpent.setScale(2, RoundingMode.HALF_UP),
            totalRemaining.setScale(2, RoundingMode.HALF_UP),
            percentageUsed,
            categoriesOverBudget,
            categoriesApproachingLimit,
            categories.stream().map(this::toCategoryResponse).toList(),
            insight
        );
    }

    // ── Internal — called by import service ───────────────────────

    @Transactional
    public void updateCategorySpending(UUID categoryId, BigDecimal amount,
                                        boolean isExpense) {
        BudgetCategory category = budgetCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (isExpense) {
            category.setSpentAmount(
                category.getSpentAmount().add(amount)
                    .setScale(2, RoundingMode.HALF_UP));
        } else {
            category.setSpentAmount(
                category.getSpentAmount().subtract(amount)
                    .max(BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP));
        }

        budgetCategoryRepository.save(category);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private Budget findBudgetForUser(UUID budgetId, UUID userId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found"));
        if (!budget.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Budget not found");
        }
        return budget;
    }

    private boolean isActiveBudget(Budget budget) {
        LocalDate today = LocalDate.now();
        boolean afterStart = !today.isBefore(budget.getStartDate());
        boolean beforeEnd = budget.getEndDate() == null ||
                !today.isAfter(budget.getEndDate());
        return afterStart && beforeEnd;
    }

    private String buildInsight(BigDecimal totalSpent, BigDecimal totalLimit,
                                 int overBudget, int approaching,
                                 List<BudgetCategory> categories) {
        if (totalLimit.compareTo(BigDecimal.ZERO) == 0) {
            return "Add categories to start tracking your budget.";
        }

        if (overBudget > 0) {
            return String.format(
                "You are over budget in %d %s. Review your spending to get back on track.",
                overBudget, overBudget == 1 ? "category" : "categories");
        }

        if (approaching > 0) {
            return String.format(
                "%d %s approaching the limit. Keep an eye on your spending.",
                approaching, approaching == 1 ? "category is" : "categories are");
        }

        BigDecimal percentageUsed = totalSpent
                .divide(totalLimit, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        if (percentageUsed.compareTo(new BigDecimal("50")) < 0) {
            return "Great job! You are well within your budget this period.";
        }

        return String.format(
            "You have used %.0f%% of your total budget. You are on track.",
            percentageUsed);
    }

    private BudgetResponse toBudgetResponse(Budget budget) {
        List<BudgetCategory> categories = budgetCategoryRepository
                .findByBudgetId(budget.getId());

        BigDecimal totalLimit = categories.stream()
                .map(BudgetCategory::getLimitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSpent = categories.stream()
                .map(BudgetCategory::getSpentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BudgetResponse(
            budget.getId(),
            budget.getName(),
            budget.getPeriodType(),
            budget.getStartDate(),
            budget.getEndDate(),
            totalLimit.setScale(2, RoundingMode.HALF_UP),
            totalSpent.setScale(2, RoundingMode.HALF_UP),
            totalLimit.subtract(totalSpent).setScale(2, RoundingMode.HALF_UP),
            isActiveBudget(budget),
            categories.stream().map(this::toCategoryResponse).toList(),
            budget.getCreatedAt()
        );
    }

    private BudgetCategoryResponse toCategoryResponse(BudgetCategory category) {
        BigDecimal remaining = category.getLimitAmount()
                .subtract(category.getSpentAmount())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal percentageUsed = category.getLimitAmount()
                .compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : category.getSpentAmount()
                .divide(category.getLimitAmount(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);

        boolean isOverBudget = category.getSpentAmount()
                .compareTo(category.getLimitAmount()) > 0;

        boolean isApproachingLimit = !isOverBudget &&
                percentageUsed.compareTo(new BigDecimal("80")) >= 0;

        return new BudgetCategoryResponse(
            category.getId(),
            category.getName(),
            category.getLimitAmount(),
            category.getSpentAmount(),
            remaining,
            percentageUsed,
            isOverBudget,
            isApproachingLimit,
            category.getCreatedAt()
        );
    }
}