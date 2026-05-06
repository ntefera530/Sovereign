package com.sovereign.domain.account.service;

import com.sovereign.config.security.UserDetailsImpl;
import com.sovereign.domain.account.dto.request.*;
import com.sovereign.domain.account.dto.response.*;
import com.sovereign.domain.account.entity.Account;
import com.sovereign.domain.account.entity.Transaction;
import com.sovereign.domain.account.entity.Transfer;
import com.sovereign.domain.account.repository.AccountRepository;
import com.sovereign.domain.account.repository.TransactionRepository;
import com.sovereign.domain.account.repository.TransferRepository;
import com.sovereign.exception.exceptions.BadRequestException;
import com.sovereign.exception.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransferRepository transferRepository;

    // ── Accounts ──────────────────────────────────────────────────

    @Transactional
    public AccountResponse createAccount(
            UserDetailsImpl userDetails, CreateAccountRequest request) {
        Account account = new Account();
        account.setUser(userDetails.getUser());
        account.setName(request.name());
        account.setType(request.type());
        account.setBalance(request.initialBalance());
        account.setCurrency(request.currency());
        accountRepository.save(account);

        log.info("Account created: {} for user: {}",
            account.getId(), userDetails.getUser().getId());
        return toAccountResponse(account);
    }

    public AccountSummaryResponse getAllAccounts(UserDetailsImpl userDetails) {
        List<Account> accounts = accountRepository
                .findByUserId(userDetails.getUser().getId());

        BigDecimal totalBalance = accounts.stream()
                .filter(Account::isActive)
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AccountSummaryResponse(
            totalBalance,
            accounts.size(),
            accounts.stream().map(this::toAccountResponse).toList()
        );
    }

    public AccountResponse getAccount(UserDetailsImpl userDetails, UUID accountId) {
        return toAccountResponse(findAccountForUser(accountId, userDetails.getUser().getId()));
    }

    @Transactional
    public AccountResponse updateAccount(
            UserDetailsImpl userDetails, UUID accountId, UpdateAccountRequest request) {
        Account account = findAccountForUser(accountId, userDetails.getUser().getId());
        account.setName(request.name());
        account.setActive(request.isActive());
        accountRepository.save(account);
        log.info("Account updated: {}", accountId);
        return toAccountResponse(account);
    }

    @Transactional
    public void deleteAccount(UserDetailsImpl userDetails, UUID accountId) {
        Account account = findAccountForUser(accountId, userDetails.getUser().getId());

        // delete all transactions first
        transactionRepository.deleteByAccountId(accountId);

        // hard delete — user owns their data
        accountRepository.delete(account);
        log.info("Account deleted: {}", accountId);
    }

    // ── Transactions ──────────────────────────────────────────────

    public List<TransactionResponse> getTransactions(
            UserDetailsImpl userDetails, UUID accountId) {
        findAccountForUser(accountId, userDetails.getUser().getId());
        return transactionRepository
                .findByAccountIdOrderByTransactionDateDesc(accountId)
                .stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    public TransactionResponse getTransaction(
            UserDetailsImpl userDetails, UUID accountId, UUID transactionId) {
        findAccountForUser(accountId, userDetails.getUser().getId());
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        return toTransactionResponse(transaction);
    }

    @Transactional
    public TransactionResponse updateTransaction(
            UserDetailsImpl userDetails, UUID accountId,
            UUID transactionId, UpdateTransactionRequest request) {
        findAccountForUser(accountId, userDetails.getUser().getId());
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        // user can only update description and category
        transaction.setDescription(request.description());
        // budget category will be set when budget domain is built
        transactionRepository.save(transaction);

        log.info("Transaction updated: {}", transactionId);
        return toTransactionResponse(transaction);
    }

    @Transactional
    public void deleteTransaction(
            UserDetailsImpl userDetails, UUID accountId, UUID transactionId) {
        Account account = findAccountForUser(accountId, userDetails.getUser().getId());
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        // reverse balance effect
        if (transaction.getType().name().equals("INCOME")) {
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        } else if (transaction.getType().name().equals("EXPENSE")) {
            account.setBalance(account.getBalance().add(transaction.getAmount()));
        }

        // hard delete — user owns their data
        transactionRepository.delete(transaction);
        accountRepository.save(account);
        log.info("Transaction deleted: {}", transactionId);
    }

    // ── Transfers ─────────────────────────────────────────────────

    public List<TransferResponse> getTransfers(UserDetailsImpl userDetails) {
        List<Account> accounts = accountRepository
                .findByUserId(userDetails.getUser().getId());

        return accounts.stream()
                .flatMap(account ->
                    transferRepository.findAllByAccountId(account.getId()).stream())
                .distinct()
                .map(this::toTransferResponse)
                .toList();
    }

    @Transactional
    public TransferResponse confirmTransfer(
            UserDetailsImpl userDetails, UUID transferId) {
        Transfer transfer = findTransferForUser(transferId, userDetails.getUser().getId());
        transfer.setUserConfirmed(true);
        transfer.setDismissed(false);
        transferRepository.save(transfer);
        log.info("Transfer confirmed: {}", transferId);
        return toTransferResponse(transfer);
    }

    @Transactional
    public TransferResponse dismissTransfer(
            UserDetailsImpl userDetails, UUID transferId) {
        Transfer transfer = findTransferForUser(transferId, userDetails.getUser().getId());
        transfer.setDismissed(true);
        transfer.setUserConfirmed(false);
        transferRepository.save(transfer);
        log.info("Transfer dismissed: {}", transferId);
        return toTransferResponse(transfer);
    }

    // ── Internal — called by import service only ──────────────────

    @Transactional
    public Transaction importTransaction(Account account, Transaction transaction) {
        // check for duplicate
        if (transactionRepository.existsByFingerprint(transaction.getFingerprint())) {
            log.warn("Duplicate transaction detected, skipping: {}",
                transaction.getFingerprint());
            return null;
        }

        // update balance
        if (transaction.getType().name().equals("INCOME")) {
            account.setBalance(account.getBalance().add(transaction.getAmount()));
        } else if (transaction.getType().name().equals("EXPENSE")) {
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        }

        transactionRepository.save(transaction);
        accountRepository.save(account);
        return transaction;
    }

    // ── Helpers ───────────────────────────────────────────────────

    private Account findAccountForUser(UUID accountId, UUID userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        if (!account.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Account not found");
        }
        return account;
    }

    private Transfer findTransferForUser(UUID transferId, UUID userId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer not found"));
        if (!transfer.getDebitTransaction().getAccount().getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Transfer not found");
        }
        return transfer;
    }

    private AccountResponse toAccountResponse(Account account) {
        return new AccountResponse(
            account.getId(),
            account.getName(),
            account.getType(),
            account.getBalance(),
            account.getCurrency(),
            account.isActive(),
            account.getCreatedAt()
        );
    }

    private TransactionResponse toTransactionResponse(Transaction transaction) {
        return new TransactionResponse(
            transaction.getId(),
            transaction.getAccount().getId(),
            transaction.getAmount(),
            transaction.getType(),
            transaction.getDescription(),
            transaction.getTransactionDate(),
            transaction.getBudgetCategory() != null
                ? transaction.getBudgetCategory().getId() : null,
            transaction.getCreatedAt()
        );
    }

    private TransferResponse toTransferResponse(Transfer transfer) {
        return new TransferResponse(
            transfer.getId(),
            transfer.getDebitTransaction().getId(),
            transfer.getDebitTransaction().getAccount().getName(),
            transfer.getCreditTransaction().getId(),
            transfer.getCreditTransaction().getAccount().getName(),
            transfer.getDebitTransaction().getAmount(),
            transfer.isAutoDetected(),
            transfer.isUserConfirmed(),
            transfer.getCreatedAt()
        );
    }
}