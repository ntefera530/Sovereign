package com.sovereign.domain.debt.service;

import com.sovereign.domain.debt.dto.response.*;
import com.sovereign.domain.debt.entity.Debt;
import com.sovereign.domain.debt.repository.DebtRepository;
import com.sovereign.exception.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoffCalculatorService {

    private static final BigDecimal MONTHS_IN_YEAR = new BigDecimal("12");
    private static final BigDecimal DAYS_IN_YEAR = new BigDecimal("365");
    private static final int MAX_MONTHS = 600; // 50 year safety cap
    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    private final DebtRepository debtRepository;

    // ── Single debt projection ─────────────────────────────────────

    public PayoffProjectionResponse project(UUID debtId, UUID userId) {
        Debt debt = findDebtForUser(debtId, userId);
        return projectDebt(debt, debt.getMinimumPayment(), true);
    }

    // ── Simulation — what if extra payment ────────────────────────

    public SimulationResponse simulate(UUID debtId, UUID userId,
                                        BigDecimal extraMonthlyPayment) {
        Debt debt = findDebtForUser(debtId, userId);

        // baseline — minimum payments only
        PayoffProjectionResponse baseline = projectDebt(
            debt, debt.getMinimumPayment(), false);

        // simulated — with extra payment
        BigDecimal simulatedPayment = debt.getMinimumPayment().add(extraMonthlyPayment);
        PayoffProjectionResponse simulated = projectDebt(debt, simulatedPayment, true);

        int monthsSaved = baseline.monthsToPayoff() - simulated.monthsToPayoff();
        BigDecimal interestSaved = baseline.totalInterestPaid()
                .subtract(simulated.totalInterestPaid());

        return new SimulationResponse(
            debt.getId(),
            debt.getName(),
            baseline.payoffDate(),
            baseline.monthsToPayoff(),
            baseline.totalInterestPaid(),
            simulated.payoffDate(),
            simulated.monthsToPayoff(),
            simulated.totalInterestPaid(),
            monthsSaved,
            interestSaved,
            extraMonthlyPayment,
            simulated.schedule()
        );
    }

    // ── Strategy comparison ───────────────────────────────────────

    public StrategyComparisonResponse compareStrategies(
            UUID userId, BigDecimal extraMonthlyPayment, List<UUID> customOrder) {

        List<Debt> debts = debtRepository.findByUserIdAndIsPaidOffFalse(userId);

        if (debts.isEmpty()) {
            throw new ResourceNotFoundException("No active debts found");
        }

        BigDecimal totalDebt = debts.stream()
                .map(Debt::getCurrentBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // run all three strategies
        StrategyResult avalanche = runStrategy(debts, extraMonthlyPayment,
            "Avalanche", "Highest interest rate first — saves the most money",
            Comparator.comparing(Debt::getInterestRate).reversed());

        StrategyResult snowball = runStrategy(debts, extraMonthlyPayment,
            "Snowball", "Smallest balance first — fastest psychological wins",
            Comparator.comparing(Debt::getCurrentBalance));

        StrategyResult custom = null;
        if (customOrder != null && !customOrder.isEmpty()) {
            custom = runStrategyCustomOrder(debts, extraMonthlyPayment, customOrder);
        }

        String recommendation = buildRecommendation(avalanche, snowball);

        return new StrategyComparisonResponse(
            extraMonthlyPayment,
            totalDebt,
            avalanche,
            snowball,
            custom,
            recommendation
        );
    }

    // ── Core amortization engine ──────────────────────────────────

    public PayoffProjectionResponse projectDebt(
            Debt debt, BigDecimal monthlyPayment, boolean includeSchedule) {

        BigDecimal monthlyRate = debt.getInterestRate()
                .divide(MONTHS_IN_YEAR, 10, RoundingMode.HALF_UP);

        BigDecimal balance = debt.getCurrentBalance();
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;

        List<AmortizationEntry> schedule = new ArrayList<>();
        LocalDate paymentDate = LocalDate.now().plusMonths(1)
                .withDayOfMonth(Math.min(debt.getDueDate().getDayOfMonth(), 28));

        int month = 0;

        while (balance.compareTo(BigDecimal.ZERO) > 0 && month < MAX_MONTHS) {
            month++;

            // calculate interest for this month
            BigDecimal interestCharge = balance.multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            // actual payment — might be less on final month
            BigDecimal payment = monthlyPayment.min(balance.add(interestCharge));

            // principal = payment - interest
            BigDecimal principalPaid = payment.subtract(interestCharge)
                    .max(BigDecimal.ZERO);

            balance = balance.subtract(principalPaid)
                    .setScale(2, RoundingMode.HALF_UP);

            totalInterest = totalInterest.add(interestCharge);
            totalPaid = totalPaid.add(payment);

            if (includeSchedule) {
                schedule.add(new AmortizationEntry(
                    month,
                    paymentDate,
                    payment,
                    principalPaid,
                    interestCharge,
                    balance.max(BigDecimal.ZERO)
                ));
            }

            paymentDate = paymentDate.plusMonths(1);
        }

        BigDecimal interestRatio = totalInterest
                .divide(debt.getCurrentBalance(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        return new PayoffProjectionResponse(
            debt.getId(),
            debt.getName(),
            debt.getCurrentBalance(),
            debt.getInterestRate(),
            monthlyPayment,
            paymentDate.minusMonths(1),
            month,
            totalInterest.setScale(2, RoundingMode.HALF_UP),
            totalPaid.setScale(2, RoundingMode.HALF_UP),
            interestRatio.setScale(2, RoundingMode.HALF_UP),
            schedule
        );
    }

    // ── Multi-debt strategy runner ────────────────────────────────

    private StrategyResult runStrategy(List<Debt> debts,
                                        BigDecimal extraPayment,
                                        String name,
                                        String description,
                                        Comparator<Debt> order) {
        // sort debts by strategy order
        List<Debt> sorted = debts.stream()
                .sorted(order)
                .collect(Collectors.toList());

        return runMultiDebtSimulation(sorted, extraPayment, name, description);
    }

    private StrategyResult runStrategyCustomOrder(List<Debt> debts,
                                                   BigDecimal extraPayment,
                                                   List<UUID> customOrder) {
        Map<UUID, Debt> debtMap = debts.stream()
                .collect(Collectors.toMap(Debt::getId, d -> d));

        List<Debt> ordered = customOrder.stream()
                .filter(debtMap::containsKey)
                .map(debtMap::get)
                .collect(Collectors.toList());

        // add any debts not in custom order at the end
        debts.stream()
                .filter(d -> !customOrder.contains(d.getId()))
                .forEach(ordered::add);

        return runMultiDebtSimulation(ordered, extraPayment,
            "Custom", "Your custom payoff order");
    }

    private StrategyResult runMultiDebtSimulation(List<Debt> orderedDebts,
                                                   BigDecimal extraPayment,
                                                   String name,
                                                   String description) {
        // track balances and paid off status
        Map<UUID, BigDecimal> balances = new LinkedHashMap<>();
        Map<UUID, BigDecimal> monthlyRates = new HashMap<>();
        Map<UUID, BigDecimal> minimumPayments = new HashMap<>();
        Map<UUID, Integer> payoffMonths = new LinkedHashMap<>();
        Map<UUID, BigDecimal> totalInterestPerDebt = new HashMap<>();

        for (Debt debt : orderedDebts) {
            balances.put(debt.getId(), debt.getCurrentBalance());
            monthlyRates.put(debt.getId(),
                debt.getInterestRate().divide(MONTHS_IN_YEAR, 10, RoundingMode.HALF_UP));
            minimumPayments.put(debt.getId(), debt.getMinimumPayment());
            totalInterestPerDebt.put(debt.getId(), BigDecimal.ZERO);
        }

        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        int month = 0;
        LocalDate currentDate = LocalDate.now();

        while (balances.values().stream().anyMatch(b -> b.compareTo(BigDecimal.ZERO) > 0)
               && month < MAX_MONTHS) {
            month++;
            currentDate = currentDate.plusMonths(1);

            // available extra payment this month
            BigDecimal remainingExtra = extraPayment;

            for (Debt debt : orderedDebts) {
                BigDecimal balance = balances.get(debt.getId());
                if (balance.compareTo(BigDecimal.ZERO) <= 0) continue;

                BigDecimal rate = monthlyRates.get(debt.getId());
                BigDecimal minimum = minimumPayments.get(debt.getId());

                // interest this month
                BigDecimal interest = balance.multiply(rate)
                        .setScale(2, RoundingMode.HALF_UP);

                // base payment — minimum or full balance if less
                BigDecimal payment = minimum.min(balance.add(interest));

                // apply extra payment to first non-paid-off debt in order
                if (remainingExtra.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal extra = remainingExtra.min(
                        balance.add(interest).subtract(payment));
                    payment = payment.add(extra);
                    remainingExtra = remainingExtra.subtract(extra);
                }

                BigDecimal principal = payment.subtract(interest).max(BigDecimal.ZERO);
                balance = balance.subtract(principal).setScale(2, RoundingMode.HALF_UP);
                balance = balance.max(BigDecimal.ZERO);

                balances.put(debt.getId(), balance);
                totalInterest = totalInterest.add(interest);
                totalPaid = totalPaid.add(payment);
                totalInterestPerDebt.merge(debt.getId(), interest, BigDecimal::add);

                if (balance.compareTo(BigDecimal.ZERO) == 0
                        && !payoffMonths.containsKey(debt.getId())) {
                    payoffMonths.put(debt.getId(), month);
                }
            }
        }

        // build payoff order list
        List<DebtPayoffOrder> payoffOrder = new ArrayList<>();
        int order = 1;
        for (Debt debt : orderedDebts) {
            Integer payoffMonth = payoffMonths.get(debt.getId());
            LocalDate payoffDate = payoffMonth != null
                ? LocalDate.now().plusMonths(payoffMonth) : null;

            payoffOrder.add(new DebtPayoffOrder(
                debt.getId(),
                debt.getName(),
                order++,
                payoffDate,
                totalInterestPerDebt.get(debt.getId())
                    .setScale(2, RoundingMode.HALF_UP)
            ));
        }

        return new StrategyResult(
            name,
            description,
            LocalDate.now().plusMonths(month),
            month,
            totalInterest.setScale(2, RoundingMode.HALF_UP),
            totalPaid.setScale(2, RoundingMode.HALF_UP),
            payoffOrder
        );
    }

    // ── Insight calculations ──────────────────────────────────────

    public BigDecimal dailyInterestCost(Debt debt) {
        return debt.getCurrentBalance()
                .multiply(debt.getInterestRate())
                .divide(DAYS_IN_YEAR, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal monthlyInterestCost(Debt debt) {
        return debt.getCurrentBalance()
                .multiply(debt.getInterestRate())
                .divide(MONTHS_IN_YEAR, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal percentagePaidOff(Debt debt) {
        if (debt.getPrincipal().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal paid = debt.getPrincipal().subtract(debt.getCurrentBalance());
        return paid.divide(debt.getPrincipal(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
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

    private String buildRecommendation(StrategyResult avalanche,
                                        StrategyResult snowball) {
        BigDecimal interestDifference = snowball.totalInterestPaid()
                .subtract(avalanche.totalInterestPaid());
        int monthDifference = snowball.totalMonths() - avalanche.totalMonths();

        if (interestDifference.compareTo(new BigDecimal("500")) < 0) {
            return String.format(
                "Both strategies are similar for your debts. " +
                "The snowball method pays off debts faster psychologically " +
                "and only costs $%.2f more in interest. " +
                "Pick whichever keeps you motivated.",
                interestDifference);
        }

        return String.format(
            "The avalanche method saves you $%.2f in interest " +
            "and gets you debt-free %d months sooner. " +
            "However, if you need motivation from quick wins, " +
            "the snowball method pays off your first debt sooner.",
            interestDifference, monthDifference);
    }
}