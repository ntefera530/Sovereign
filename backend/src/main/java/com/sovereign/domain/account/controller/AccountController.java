package com.sovereign.domain.account.controller;

import com.sovereign.config.security.UserDetailsImpl;
import com.sovereign.domain.account.dto.request.*;
import com.sovereign.domain.account.dto.response.*;
import com.sovereign.domain.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(userDetails, request));
    }

    @GetMapping
    public ResponseEntity<AccountSummaryResponse> getAllAccounts(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(accountService.getAllAccounts(userDetails));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getAccount(userDetails, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccount(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountRequest request) {
        return ResponseEntity.ok(accountService.updateAccount(userDetails, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {
        accountService.deleteAccount(userDetails, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getTransactions(userDetails, id));
    }

    @GetMapping("/{id}/transactions/{txId}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id,
            @PathVariable UUID txId) {
        return ResponseEntity.ok(accountService.getTransaction(userDetails, id, txId));
    }

    @PutMapping("/{id}/transactions/{txId}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id,
            @PathVariable UUID txId,
            @Valid @RequestBody UpdateTransactionRequest request) {
        return ResponseEntity.ok(
            accountService.updateTransaction(userDetails, id, txId, request));
    }

    @DeleteMapping("/{id}/transactions/{txId}")
    public ResponseEntity<Void> deleteTransaction(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id,
            @PathVariable UUID txId) {
        accountService.deleteTransaction(userDetails, id, txId);
        return ResponseEntity.noContent().build();
    }
}