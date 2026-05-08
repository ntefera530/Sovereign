package com.sovereign.domain.budget.controller;

import com.sovereign.config.security.UserDetailsImpl;
import com.sovereign.domain.budget.dto.request.*;
import com.sovereign.domain.budget.dto.response.*;
import com.sovereign.domain.budget.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CreateBudgetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(budgetService.createBudget(userDetails, request));
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getAllBudgets(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(budgetService.getAllBudgets(userDetails));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getBudget(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(budgetService.getBudget(userDetails, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBudgetRequest request) {
        return ResponseEntity.ok(budgetService.updateBudget(userDetails, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {
        budgetService.deleteBudget(userDetails, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/categories")
    public ResponseEntity<BudgetCategoryResponse> createCategory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(budgetService.createCategory(userDetails, id, request));
    }

    @GetMapping("/{id}/categories")
    public ResponseEntity<List<BudgetCategoryResponse>> getCategories(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(budgetService.getCategories(userDetails, id));
    }

    @PutMapping("/{id}/categories/{catId}")
    public ResponseEntity<BudgetCategoryResponse> updateCategory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id,
            @PathVariable UUID catId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(
            budgetService.updateCategory(userDetails, id, catId, request));
    }

    @DeleteMapping("/{id}/categories/{catId}")
    public ResponseEntity<Void> deleteCategory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id,
            @PathVariable UUID catId) {
        budgetService.deleteCategory(userDetails, id, catId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<BudgetSummaryResponse> getSummary(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(budgetService.getSummary(userDetails, id));
    }
}