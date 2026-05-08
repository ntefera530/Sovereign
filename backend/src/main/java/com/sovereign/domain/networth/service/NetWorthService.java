package com.sovereign.domain.networth.service;

import com.sovereign.common.enums.AccountType;
import com.sovereign.common.enums.NetWorthCalculationType;
import com.sovereign.common.enums.NetWorthSourceType;
import com.sovereign.config.security.UserDetailsImpl;
import com.sovereign.domain.account.entity.Account;
import com.sovereign.domain.account.repository.AccountRepository;
import com.sovereign.domain.debt.entity.Debt;
import com.sovereign.domain.debt.repository.DebtRepository;
import com.sovereign.domain.networth.dto.request.CreateAssetRequest;
import com.sovereign.domain.networth.dto.request.NetWorthRequest;
import com.sovereign.domain.networth.dto.request.UpdateAssetRequest;
import com.sovereign.domain.networth.dto.response.*;
import com.sovereign.domain.networth.entity.ManualAsset;
import com.sovereign.domain.networth.entity.NetWorthAssetEntry;
import com.sovereign.domain.networth.entity.NetWorthSnapshot;
import com.sovereign.domain.networth.repository.ManualAssetRepository;
import com.sovereign.domain.networth.repository.NetWorthAssetEntryRepository;
import com.sovereign.domain.networth.repository.NetWorthSnapshotRepository;
import com.sovereign.exception.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NetWorthService {

    private final AccountRepository accountRepository;
    private final DebtRepository debtRepository;
    private final ManualAssetRepository manualAssetRepository;
    private final NetWorthSnapshotRepository snapshotRepository;
    private final NetWorthAssetEntryRepository assetEntryRepository;

    // ── Calculate current net worth ───────────────────────────────

    public NetWorthResponse calculate(UserDetailsImpl userDetails,
                                       NetWorthRequest request) {
        UUID userId = userDetails.getUser().getId();

        List<Account> accounts = accountRepository
                .findByUserIdAndIsActiveTrue(userId);
        List<Debt> debts = debtRepository
                .findByUserIdAndIsPaidOffFalse(userId);
        List<ManualAsset> manualAssets = manualAssetRepository
                .findByUserId(userId);

        return calculateNetWorth(accounts, debts, manualAssets,
            request.calculationType(), request.customAccountIds());
    }

    // ── Snapshot ──────────────────────────────────────────────────

    @Transactional
    public NetWorthSnapshotResponse takeSnapshot(UserDetailsImpl userDetails,
                                                  NetWorthCalculationType type) {
        UUID userId = userDetails.getUser().getId();

        List<Account> accounts = accountRepository
                .findByUserIdAndIsActiveTrue(userId);
        List<Debt> debts = debtRepository
                .findByUserIdAndIsPaidOffFalse(userId);
        List<ManualAsset> manualAssets = manualAssetRepository
                .findByUserId(userId);

        NetWorthResponse current = calculateNetWorth(
            accounts, debts, manualAssets, type, null);

        // save snapshot
        NetWorthSnapshot snapshot = new NetWorthSnapshot();
        snapshot.setUser(userDetails.getUser());
        snapshot.setCalculationType(type);
        snapshot.setTotalAssets(current.totalAssets());
        snapshot.setTotalLiabilities(current.totalLiabilities());
        snapshot.setNetWorth(current.netWorth());
        snapshot.setSnapshotDate(LocalDateTime.now());
        snapshotRepository.save(snapshot);

        // save individual entries — no FK enforcement intentional
        List<NetWorthAssetEntry> entries = new ArrayList<>();

        for (AssetBreakdown asset : current.assetBreakdown()) {
            NetWorthAssetEntry entry = new NetWorthAssetEntry();
            entry.setSnapshotId(snapshot.getId());
            entry.setSourceType(asset.sourceType());
            entry.setSourceId(asset.sourceId());
            entry.setName(asset.name());
            entry.setValue(asset.value());
            entries.add(entry);
        }

        for (AssetBreakdown liability : current.liabilityBreakdown()) {
            NetWorthAssetEntry entry = new NetWorthAssetEntry();
            entry.setSnapshotId(snapshot.getId());
            entry.setSourceType(liability.sourceType());
            entry.setSourceId(liability.sourceId());
            entry.setName(liability.name());
            entry.setValue(liability.value().negate());
            entries.add(entry);
        }

        assetEntryRepository.saveAll(entries);

        log.info("Net worth snapshot taken for user: {}", userId);
        return toSnapshotResponse(snapshot);
    }

    // ── History ───────────────────────────────────────────────────

    public NetWorthHistoryResponse getHistory(UserDetailsImpl userDetails) {
        List<NetWorthSnapshot> snapshots = snapshotRepository
                .findByUserIdOrderBySnapshotDateDesc(
                    userDetails.getUser().getId());

        if (snapshots.isEmpty()) {
            return new NetWorthHistoryResponse(
                List.of(), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, "No history yet"
            );
        }

        List<NetWorthSnapshotResponse> responses = snapshots.stream()
                .map(this::toSnapshotResponse)
                .toList();

        BigDecimal newest = snapshots.get(0).getNetWorth();
        BigDecimal oldest = snapshots.get(snapshots.size() - 1).getNetWorth();
        BigDecimal changeFromFirst = newest.subtract(oldest);
        BigDecimal changeFromLast = snapshots.size() > 1
            ? newest.subtract(snapshots.get(1).getNetWorth())
            : BigDecimal.ZERO;

        BigDecimal highest = snapshots.stream()
                .map(NetWorthSnapshot::getNetWorth)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal lowest = snapshots.stream()
                .map(NetWorthSnapshot::getNetWorth)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        String trend = buildTrend(changeFromFirst, snapshots.size());

        return new NetWorthHistoryResponse(
            responses,
            changeFromFirst.setScale(2, RoundingMode.HALF_UP),
            changeFromLast.setScale(2, RoundingMode.HALF_UP),
            highest.setScale(2, RoundingMode.HALF_UP),
            lowest.setScale(2, RoundingMode.HALF_UP),
            trend
        );
    }

    // ── Manual assets ─────────────────────────────────────────────

    public List<ManualAssetResponse> getAssets(UserDetailsImpl userDetails) {
        return manualAssetRepository
                .findByUserId(userDetails.getUser().getId())
                .stream()
                .map(this::toAssetResponse)
                .toList();
    }

    @Transactional
    public ManualAssetResponse createAsset(UserDetailsImpl userDetails,
                                            CreateAssetRequest request) {
        ManualAsset asset = new ManualAsset();
        asset.setUser(userDetails.getUser());
        asset.setName(request.name());
        asset.setType(request.type());
        asset.setValue(request.value());
        asset.setCurrency(request.currency());
        asset.setValuationDate(request.valuationDate());
        asset.setNotes(request.notes());
        manualAssetRepository.save(asset);

        log.info("Manual asset created: {} for user: {}",
            asset.getId(), userDetails.getUser().getId());
        return toAssetResponse(asset);
    }

    @Transactional
    public ManualAssetResponse updateAsset(UserDetailsImpl userDetails,
                                            UUID assetId, UpdateAssetRequest request) {
        ManualAsset asset = findAssetForUser(assetId, userDetails.getUser().getId());
        asset.setValue(request.value());
        asset.setValuationDate(request.valuationDate());
        asset.setNotes(request.notes());
        manualAssetRepository.save(asset);

        log.info("Manual asset updated: {}", assetId);
        return toAssetResponse(asset);
    }

    @Transactional
    public void deleteAsset(UserDetailsImpl userDetails, UUID assetId) {
        ManualAsset asset = findAssetForUser(assetId, userDetails.getUser().getId());
        manualAssetRepository.delete(asset);
        log.info("Manual asset deleted: {}", assetId);
    }

    // ── Core calculation engine ───────────────────────────────────

    private NetWorthResponse calculateNetWorth(
            List<Account> accounts,
            List<Debt> debts,
            List<ManualAsset> manualAssets,
            NetWorthCalculationType type,
            List<UUID> customAccountIds) {

        List<Account> filteredAccounts = filterAccounts(accounts, type, customAccountIds);

        List<AssetBreakdown> assetBreakdown = new ArrayList<>();
        List<AssetBreakdown> liabilityBreakdown = new ArrayList<>();

        // accounts
        BigDecimal totalAccountBalance = BigDecimal.ZERO;
        for (Account account : filteredAccounts) {
            totalAccountBalance = totalAccountBalance.add(account.getBalance());
            assetBreakdown.add(new AssetBreakdown(
                account.getId(),
                NetWorthSourceType.ACCOUNT.name(),
                account.getName(),
                account.getBalance()
            ));
        }

        // manual assets — only included in TOTAL and CUSTOM
        BigDecimal totalManualAssets = BigDecimal.ZERO;
        if (type == NetWorthCalculationType.TOTAL ||
                type == NetWorthCalculationType.CUSTOM ||
                type == NetWorthCalculationType.EXCLUDING_REAL_ESTATE) {

            for (ManualAsset asset : manualAssets) {
                // skip real estate if excluding
                if (type == NetWorthCalculationType.EXCLUDING_REAL_ESTATE &&
                        asset.getType().name().equals("REAL_ESTATE")) {
                    continue;
                }
                totalManualAssets = totalManualAssets.add(asset.getValue());
                assetBreakdown.add(new AssetBreakdown(
                    asset.getId(),
                    NetWorthSourceType.MANUAL_ASSET.name(),
                    asset.getName(),
                    asset.getValue()
                ));
            }
        }

        // debts as liabilities — only in TOTAL
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        if (type == NetWorthCalculationType.TOTAL ||
                type == NetWorthCalculationType.CUSTOM) {
            for (Debt debt : debts) {
                totalLiabilities = totalLiabilities.add(debt.getCurrentBalance());
                liabilityBreakdown.add(new AssetBreakdown(
                    debt.getId(),
                    NetWorthSourceType.DEBT.name(),
                    debt.getName(),
                    debt.getCurrentBalance()
                ));
            }
        }

        BigDecimal totalAssets = totalAccountBalance.add(totalManualAssets);
        BigDecimal netWorth = totalAssets.subtract(totalLiabilities);

        // liquid assets — checking + savings only
        BigDecimal liquidAssets = accounts.stream()
                .filter(a -> a.getType() == AccountType.CHECKING ||
                             a.getType() == AccountType.SAVINGS)
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // cash on hand — checking + savings + cash
        BigDecimal cashOnHand = accounts.stream()
                .filter(a -> a.getType() == AccountType.CHECKING ||
                             a.getType() == AccountType.SAVINGS ||
                             a.getType() == AccountType.CASH)
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new NetWorthResponse(
            type,
            totalAssets.setScale(2, RoundingMode.HALF_UP),
            totalLiabilities.setScale(2, RoundingMode.HALF_UP),
            netWorth.setScale(2, RoundingMode.HALF_UP),
            liquidAssets.setScale(2, RoundingMode.HALF_UP),
            cashOnHand.setScale(2, RoundingMode.HALF_UP),
            assetBreakdown,
            liabilityBreakdown,
            LocalDateTime.now()
        );
    }

    private List<Account> filterAccounts(List<Account> accounts,
                                          NetWorthCalculationType type,
                                          List<UUID> customAccountIds) {
        return switch (type) {
            case TOTAL -> accounts;
            case LIQUID -> accounts.stream()
                    .filter(a -> a.getType() == AccountType.CHECKING ||
                                 a.getType() == AccountType.SAVINGS ||
                                 a.getType() == AccountType.INVESTMENT)
                    .collect(Collectors.toList());
            case CASH_ONLY -> accounts.stream()
                    .filter(a -> a.getType() == AccountType.CHECKING ||
                                 a.getType() == AccountType.SAVINGS ||
                                 a.getType() == AccountType.CASH)
                    .collect(Collectors.toList());
            case EXCLUDING_REAL_ESTATE -> accounts;
            case CUSTOM -> accounts.stream()
                    .filter(a -> customAccountIds != null &&
                                 customAccountIds.contains(a.getId()))
                    .collect(Collectors.toList());
        };
    }

    // ── Helpers ───────────────────────────────────────────────────

    private ManualAsset findAssetForUser(UUID assetId, UUID userId) {
        ManualAsset asset = manualAssetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
        if (!asset.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Asset not found");
        }
        return asset;
    }

    private String buildTrend(BigDecimal change, int snapshotCount) {
        if (snapshotCount < 2) return "Not enough history to determine trend";

        if (change.compareTo(BigDecimal.ZERO) > 0) {
            return String.format("Your net worth has grown by $%.2f since tracking began",
                change);
        } else if (change.compareTo(BigDecimal.ZERO) < 0) {
            return String.format("Your net worth has decreased by $%.2f since tracking began",
                change.abs());
        }
        return "Your net worth has remained stable";
    }

    private NetWorthSnapshotResponse toSnapshotResponse(NetWorthSnapshot snapshot) {
        return new NetWorthSnapshotResponse(
            snapshot.getId(),
            snapshot.getCalculationType(),
            snapshot.getTotalAssets(),
            snapshot.getTotalLiabilities(),
            snapshot.getNetWorth(),
            snapshot.getSnapshotDate()
        );
    }

    private ManualAssetResponse toAssetResponse(ManualAsset asset) {
        return new ManualAssetResponse(
            asset.getId(),
            asset.getName(),
            asset.getType(),
            asset.getValue(),
            asset.getCurrency(),
            asset.getValuationDate(),
            asset.getNotes(),
            asset.getCreatedAt()
        );
    }
}