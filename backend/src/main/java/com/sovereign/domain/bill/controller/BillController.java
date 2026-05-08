package com.sovereign.domain.bill.controller;

import com.sovereign.domain.bill.dto.request.BillSplitRequest;
import com.sovereign.domain.bill.dto.response.BillSplitResponse;
import com.sovereign.domain.bill.service.BillSplitterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillSplitterService billSplitterService;

    @PostMapping("/split")
    public ResponseEntity<BillSplitResponse> split(
            @Valid @RequestBody BillSplitRequest request) {
        return ResponseEntity.ok(billSplitterService.calculate(request));
    }
}