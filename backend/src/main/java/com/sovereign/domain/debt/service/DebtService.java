package com.sovereign.domain.debt.service;

import com.sovereign.config.security.UserDetailsImpl;
import com.sovereign.domain.debt.dto.request.*;
import com.sovereign.domain.debt.dto.response.*;
import com.sovereign.domain.debt.entity.Debt;
import com.sovereign.domain.debt.entity.DebtPayment;
import com.sovereign.domain.debt.repository.DebtPaymentRepository;
import com.sovereign.domain.debt.repository.DebtRepository;
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
public class DebtService {

    private final DebtRepository debtRepository;
    private final DebtPaymentRepository debtPaymentRepository;
    private final PayoffCalculatorService calculator;

    // ── Debt CRUD ─────────────────────────────────────────────────

    @Transactional
    public DebtResponse createDebt(UserDetailsImpl userDetails,
                                    CreateDebtRequest request) {
        Debt debt = new Debt();
        debt.setUser(userDetails.getUser());
        debt.setName(request.name());
        debt.setDebtType(request.debtType());
        debt.setPrincipal(request.principal());
        debt.setCurrentBalance(request.currentBalance());
        debt.setInterestRate(request.interestRate());
        debt.setMinimumPayment(request.minimumPayment());
        debt.setPaymentFrequency(request.paymentFrequency());
        debt.setDueDate(request.dueDate());
        debtRepository.save(debt);

        log.info("Debt created: {} for user: {}",
            debt.getId(), userDetails.getUser().getId());
        return toDebtResponse(debt);
    }

    public List<DebtResponse> getAllDebts(UserDetailsImpl userDetails) {
        return debtRepository
                .findByUserId(userDetails.getUser().getId())
                .stream()
                .map(this::toDebtResponse)
                .toList();
    }

    public DebtResponse getDebt(UserDetailsImpl userDetails, UUID debtId) {
        return toDebtResponse(findDebtForUser(debtId, userDetails.getUser().getId()));
    }

    @Transactional
    public DebtResponse updateDebt(UserDetailsImpl userDetails,
                                    UUID debtId, UpdateDebtRequest request) {
        Debt debt = findDebtForUser(debtId, userDetails.getUser().getId());

        debt.setName(request.name());
        debt.setCurrentBalance(request.currentBalance());
        debt.setInterestRate(request.interestRate());
        debt.setMinimumPayment(request.minimumPayment());
        debt.setPaymentFrequency(request.paymentFrequency());
        debt.setDueDate(request.dueDate());

        // check if debt is now paid off
        if (request.currentBalance().compareTo(BigDecimal.ZERO) == 0) {
            debt.setPaidOff(true);
            log.info("Debt marked as paid off: {}", debtId);
        }

        debtRepository.save(debt);
        return toDebtResponse(debt);
    }

    @Transactional
    public void deleteDebt(UserDetailsImpl userDetails, UUID debtId) {
        Debt debt = findDebtForUser(debtId, userDetails.getUser().getId());
        debtPaymentRepository.deleteByDebtId(debtId);
        debtRepository.delete(debt);
        log.info("Debt deleted: {}", debtId);
    }

    // ── Payments ──────────────────────────────────────────────────

    @Transactional
    public DebtPaymentResponse addPayment(UserDetailsImpl userDetails,
                                           UUID debtId, AddPaymentRequest request) {
        Debt debt = findDebtForUser(debtId, userDetails.getUser().getId());

        if (debt.isPaidOff()) {
            throw new BadRequestException("This debt is already paid off");
        }

        // extra amount = total payment - minimum payment
        BigDecimal extraAmount = request.extraAmount() != null
            ? request.extraAmount()
            : request.amount().subtract(debt.getMinimumPayment()).max(BigDecimal.ZERO);

        DebtPayment payment = new DebtPayment();
        payment.setDebt(debt);
        payment.setAmount(request.amount());
        payment.setExtraAmount(extraAmount);
        payment.setPaymentDate(request.paymentDate());
        payment.setNote(request.note());
        debtPaymentRepository.save(payment);

        // update current balance
        BigDecimal newBalance = debt.getCurrentBalance()
                .subtract(request.amount())
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        debt.setCurrentBalance(newBalance);

        // mark paid off if balance reaches zero
        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            debt.setPaidOff(true);
            log.info("Debt fully paid off: {}", debtId);
        }

        debtRepository.save(debt);
        log.info("Payment recorded on debt: {}", debtId);
        return toPaymentResponse(payment);
    }

    public List<DebtPaymentResponse> getPayments(UserDetailsImpl userDetails,
                                                   UUID debtId) {
        findDebtForUser(debtId, userDetails.getUser().getId());
        return debtPaymentRepository
                .findByDebtIdOrderByPaymentDateDesc(debtId)
                .stream()
                .map(this::toPaymentResponse)
                .toList();
    }

    @Transactional
    public void deletePayment(UserDetailsImpl userDetails,
                               UUID debtId, UUID paymentId) {
        Debt debt = findDebtForUser(debtId, userDetails.getUser().getId());
        DebtPayment payment = debtPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        // restore balance
        debt.setCurrentBalance(
            debt.getCurrentBalance().add(payment.getAmount())
        );

        // un-mark paid off if balance restored
        if (debt.isPaidOff()) {
            debt.setPaidOff(false);
        }

        debtPaymentRepository.delete(payment);
        debtRepository.save(debt);
        log.info("Payment deleted: {} on debt: {}", paymentId, debtId);
    }

    // ── Summary ───────────────────────────────────────────────────

    public DebtSummaryResponse getSummary(UserDetailsImpl userDetails) {
        List<Debt> activeDebts = debtRepository
                .findByUserIdAndIsPaidOffFalse(userDetails.getUser().getId());

        if (activeDebts.isEmpty()) {
            return new DebtSummaryResponse(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, LocalDate.now(), 0, BigDecimal.ZERO, List.of()
            );
        }

        BigDecimal totalDebt = activeDebts.stream()
                .map(Debt::getCurrentBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalMinimumPayments = activeDebts.stream()
                .map(Debt::getMinimumPayment)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalMonthlyInterest = activeDebts.stream()
                .map(calculator::monthlyInterestCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDailyInterest = activeDebts.stream()
                .map(calculator::dailyInterestCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // estimate debt free date at minimums only
        // use the longest payoff time across all debts
        int maxMonths = activeDebts.stream()
                .mapToInt(debt -> calculator
                    .projectDebt(debt, debt.getMinimumPayment(), false)
                    .monthsToPayoff())
                .max()
                .orElse(0);

        BigDecimal totalInterestIfMinimums = activeDebts.stream()
                .map(debt -> calculator
                    .projectDebt(debt, debt.getMinimumPayment(), false)
                    .totalInterestPaid())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DebtSummaryResponse(
            totalDebt.setScale(2, RoundingMode.HALF_UP),
            totalMinimumPayments.setScale(2, RoundingMode.HALF_UP),
            totalMonthlyInterest.setScale(2, RoundingMode.HALF_UP),
            totalDailyInterest.setScale(2, RoundingMode.HALF_UP),
            LocalDate.now().plusMonths(maxMonths),
            maxMonths,
            totalInterestIfMinimums.setScale(2, RoundingMode.HALF_UP),
            activeDebts.stream().map(this::toDebtResponse).toList()
        );
    }

    // ── Helpers ───────────────────────────────────────────────────

    private Debt findDebtForUser(UUID debtId, UUID userId) {
        Debt debt = debtRepository.findById(debtId)
                .orElseThrow(() -> new ResourceNotFoundException("Debt not found"));
        if (!debt.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Debt not found");
        }
        return debt;
    }

    private DebtResponse toDebtResponse(Debt debt) {
        return new DebtResponse(
            debt.getId(),
            debt.getName(),
            debt.getDebtType(),
            debt.getPrincipal(),
            debt.getCurrentBalance(),
            debt.getInterestRate(),
            debt.getMinimumPayment(),
            debt.getPaymentFrequency(),
            debt.getDueDate(),
            debt.isPaidOff(),
            calculator.dailyInterestCost(debt),
            calculator.monthlyInterestCost(debt),
            calculator.percentagePaidOff(debt),
            debt.getCreatedAt()
        );
    }

    private DebtPaymentResponse toPaymentResponse(DebtPayment payment) {
        return new DebtPaymentResponse(
            payment.getId(),
            payment.getDebt().getId(),
            payment.getAmount(),
            payment.getExtraAmount(),
            payment.getPaymentDate(),
            payment.getNote(),
            payment.getCreatedAt()
        );
    }
}