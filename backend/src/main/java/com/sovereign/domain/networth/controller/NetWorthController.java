package com.sovereign.domain.networth.controller;

import com.sovereign.common.enums.NetWorthCalculationType;
import com.sovereign.config.security.UserDetailsImpl;
import com.sovereign.domain.networth.dto.request.CreateAssetRequest;
import com.sovereign.domain.networth.dto.request.NetWorthRequest;
import com.sovereign.domain.networth.dto.request.UpdateAssetRequest;
import com.sovereign.domain.networth.dto.response.*;
import com.sovereign.domain.networth.service.NetWorthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/networth")
@RequiredArgsConstructor
public class NetWorthController {

    private final NetWorthService netWorthService;

    @PostMapping
    public ResponseEntity<NetWorthResponse> calculate(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody NetWorthRequest request) {
        return ResponseEntity.ok(netWorthService.calculate(userDetails, request));
    }

    @PostMapping("/snapshot")
    public ResponseEntity<NetWorthSnapshotResponse> takeSnapshot(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "TOTAL") NetWorthCalculationType type) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(netWorthService.takeSnapshot(userDetails, type));
    }

    @GetMapping("/history")
    public ResponseEntity<NetWorthHistoryResponse> getHistory(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(netWorthService.getHistory(userDetails));
    }

    @GetMapping("/assets")
    public ResponseEntity<List<ManualAssetResponse>> getAssets(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(netWorthService.getAssets(userDetails));
    }

    @PostMapping("/assets")
    public ResponseEntity<ManualAssetResponse> createAsset(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CreateAssetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(netWorthService.createAsset(userDetails, request));
    }

    @PutMapping("/assets/{id}")
    public ResponseEntity<ManualAssetResponse> updateAsset(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAssetRequest request) {
        return ResponseEntity.ok(netWorthService.updateAsset(userDetails, id, request));
    }

    @DeleteMapping("/assets/{id}")
    public ResponseEntity<Void> deleteAsset(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable UUID id) {
        netWorthService.deleteAsset(userDetails, id);
        return ResponseEntity.noContent().build();
    }
}