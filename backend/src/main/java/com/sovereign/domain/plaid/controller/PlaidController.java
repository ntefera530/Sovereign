package com.sovereign.domain.plaid.controller;

import com.sovereign.config.security.UserDetailsImpl;
import com.sovereign.domain.plaid.dto.request.ExchangePublicTokenRequest;
import com.sovereign.domain.plaid.dto.response.LinkTokenResponse;
import com.sovereign.domain.plaid.dto.response.PlaidItemResponse;
import com.sovereign.domain.plaid.service.PlaidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/plaid")
@RequiredArgsConstructor
public class PlaidController {

    private final PlaidService plaidService;

    @PostMapping("/link-token")
    public ResponseEntity<LinkTokenResponse> createLinkToken(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(plaidService.createLinkToken(userDetails));
    }

    @PostMapping("/exchange-public-token")
    public ResponseEntity<List<PlaidItemResponse>> exchangePublicToken(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody ExchangePublicTokenRequest request) {
        return ResponseEntity.ok(plaidService.exchangePublicToken(userDetails, request));
    }

    @GetMapping("/items")
    public ResponseEntity<List<PlaidItemResponse>> getItems(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(plaidService.getItems(userDetails));
    }

    @PostMapping("/items/{id}/sync")
    public ResponseEntity<Void> sync(
            @AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable UUID id) {
        plaidService.syncTransactions(userDetails, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> removeItem(
            @AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable UUID id) {
        plaidService.removeItem(userDetails, id);
        return ResponseEntity.noContent().build();
    }
}