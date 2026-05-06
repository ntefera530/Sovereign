package com.sovereign.domain.account.controller;

import com.sovereign.config.security.UserDetailsImpl;
import com.sovereign.domain.account.dto.response.TransferResponse;
import com.sovereign.domain.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<List<TransferResponse>> getTransfers(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(accountService.getTransfers(userDetails));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<TransferResponse> confirmTransfer(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(accountService.confirmTransfer(userDetails, id));
    }

    @PutMapping("/{id}/dismiss")
    public ResponseEntity<TransferResponse> dismissTransfer(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(accountService.dismissTransfer(userDetails, id));
    }
}