package com.sovereign.domain.debt.controller;

import com.sovereign.config.security.UserDetailsImpl;
import com.sovereign.domain.debt.dto.request.*;
import com.sovereign.domain.debt.dto.response.*;
import com.sovereign.domain.debt.service.DebtService;
import com.sovereign.domain.debt.service.PayoffCalculatorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/debts")
@RequiredArgsConstructor
public class DebtController {

    private final DebtService debtService;
    private final PayoffCalculatorService calculatorService;

    // ── Debt CRUD ─────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<DebtResponse> createDebt(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CreateDebtRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(debtService.createDebt(userDetails, request));
    }

    @GetMapping
    public ResponseEntity<List<DebtResponse>> getAllDebts(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(debtService.getAllDebts(userDetails));
    }

    @GetMapping("/summary")
    public ResponseEntity<DebtSummaryResponse> getSummary(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(debtService.getSummary(userDetails));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DebtResponse> getDebt(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(debtService.getDebt(userDetails, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DebtResponse> updateDebt(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDebtRequest request) {
        return ResponseEntity.ok(debtService.updateDebt(userDetails, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDebt(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {
        debtService.deleteDebt(userDetails, id);
        return ResponseEntity.noContent().build();
    }

    // ── Payments ──────────────────────────────────────────────────

    @PostMapping("/{id}/payments")
    public ResponseEntity<DebtPaymentResponse> addPayment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody AddPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(debtService.addPayment(userDetails, id, request));
    }

    @GetMapping("/{id}/payments")
    public ResponseEntity<List<DebtPaymentResponse>> getPayments(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(debtService.getPayments(userDetails, id));
    }

    @DeleteMapping("/{id}/payments/{paymentId}")
    public ResponseEntity<Void> deletePayment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id,
            @PathVariable UUID paymentId) {
        debtService.deletePayment(userDetails, id, paymentId);
        return ResponseEntity.noContent().build();
    }

    // ── Calculator ────────────────────────────────────────────────

    @GetMapping("/{id}/projection")
    public ResponseEntity<PayoffProjectionResponse> getProjection(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(
            calculatorService.project(id, userDetails.getUser().getId()));
    }

    @PostMapping("/simulate")
    public ResponseEntity<SimulationResponse> simulate(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody SimulatePayoffRequest request) {
        return ResponseEntity.ok(calculatorService.simulate(
            request.debtId(),
            userDetails.getUser().getId(),
            request.extraMonthlyPayment()
        ));
    }

    @PostMapping("/strategies")
    public ResponseEntity<StrategyComparisonResponse> compareStrategies(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CompareStrategiesRequest request) {
        return ResponseEntity.ok(calculatorService.compareStrategies(
            userDetails.getUser().getId(),
            request.extraMonthlyPayment(),
            request.customOrder()
        ));
    }
}