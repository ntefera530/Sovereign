package com.sovereign.domain.bill.service;

import com.sovereign.domain.bill.dto.request.BillParticipant;
import com.sovereign.domain.bill.dto.request.BillSplitRequest;
import com.sovereign.domain.bill.dto.response.BillSplitResponse;
import com.sovereign.domain.bill.dto.response.ParticipantShare;
import com.sovereign.exception.exceptions.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class BillSplitterService {

    public BillSplitResponse calculate(BillSplitRequest request) {
        BigDecimal subtotal = request.subtotal();
        BigDecimal tipPercent = request.tipPercent()
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal taxPercent = request.taxPercent() != null
                ? request.taxPercent().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal tipAmount = subtotal.multiply(tipPercent)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = subtotal.multiply(taxPercent)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalBill = subtotal.add(tipAmount).add(taxAmount);

        List<ParticipantShare> shares = splitAmongParticipants(
            request.participants(), subtotal, tipAmount, taxAmount);

        // validate shares add up to total
        BigDecimal sharesTotal = shares.stream()
                .map(ParticipantShare::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // handle rounding differences — add remainder to first participant
        BigDecimal remainder = totalBill.subtract(sharesTotal);
        if (remainder.abs().compareTo(new BigDecimal("0.10")) > 0) {
            throw new BadRequestException(
                "Bill split calculation error — please check item amounts");
        }

        if (remainder.compareTo(BigDecimal.ZERO) != 0 && !shares.isEmpty()) {
            ParticipantShare first = shares.get(0);
            shares.set(0, new ParticipantShare(
                first.name(),
                first.itemsTotal(),
                first.tipShare(),
                first.taxShare(),
                first.total().add(remainder)
            ));
        }

        return new BillSplitResponse(
            subtotal,
            request.tipPercent(),
            tipAmount,
            request.taxPercent() != null ? request.taxPercent() : BigDecimal.ZERO,
            taxAmount,
            totalBill,
            shares
        );
    }

    private List<ParticipantShare> splitAmongParticipants(
            List<BillParticipant> participants,
            BigDecimal subtotal,
            BigDecimal tipAmount,
            BigDecimal taxAmount) {

        // check if any participant has items
        boolean hasItemizedParticipants = participants.stream()
                .anyMatch(p -> p.items() != null && !p.items().isEmpty());

        if (hasItemizedParticipants) {
            return splitByItems(participants, subtotal, tipAmount, taxAmount);
        } else {
            return splitEqually(participants, subtotal, tipAmount, taxAmount);
        }
    }

    private List<ParticipantShare> splitEqually(
            List<BillParticipant> participants,
            BigDecimal subtotal,
            BigDecimal tipAmount,
            BigDecimal taxAmount) {

        int count = participants.size();
        BigDecimal countBD = new BigDecimal(count);

        BigDecimal equalSubtotal = subtotal
                .divide(countBD, 2, RoundingMode.HALF_UP);
        BigDecimal equalTip = tipAmount
                .divide(countBD, 2, RoundingMode.HALF_UP);
        BigDecimal equalTax = taxAmount
                .divide(countBD, 2, RoundingMode.HALF_UP);
        BigDecimal equalTotal = equalSubtotal.add(equalTip).add(equalTax);

        List<ParticipantShare> shares = new ArrayList<>();
        for (BillParticipant participant : participants) {
            shares.add(new ParticipantShare(
                participant.name(),
                equalSubtotal,
                equalTip,
                equalTax,
                equalTotal
            ));
        }
        return shares;
    }

    private List<ParticipantShare> splitByItems(
            List<BillParticipant> participants,
            BigDecimal subtotal,
            BigDecimal tipAmount,
            BigDecimal taxAmount) {

        // calculate each person's item total
        List<BigDecimal> itemTotals = participants.stream()
                .map(p -> p.items() == null ? BigDecimal.ZERO
                    : p.items().stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .toList();

        // validate item totals add up to subtotal
        BigDecimal itemsSum = itemTotals.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (itemsSum.subtract(subtotal).abs()
                .compareTo(new BigDecimal("0.10")) > 0) {
            throw new BadRequestException(
                String.format(
                    "Item totals ($%.2f) do not match subtotal ($%.2f)",
                    itemsSum, subtotal));
        }

        List<ParticipantShare> shares = new ArrayList<>();
        for (int i = 0; i < participants.size(); i++) {
            BillParticipant participant = participants.get(i);
            BigDecimal itemTotal = itemTotals.get(i);

            // tip and tax proportional to item total
            BigDecimal proportion = subtotal.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : itemTotal.divide(subtotal, 6, RoundingMode.HALF_UP);

            BigDecimal tipShare = tipAmount.multiply(proportion)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal taxShare = taxAmount.multiply(proportion)
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal total = itemTotal.add(tipShare).add(taxShare);

            shares.add(new ParticipantShare(
                participant.name(),
                itemTotal.setScale(2, RoundingMode.HALF_UP),
                tipShare,
                taxShare,
                total
            ));
        }

        return shares;
    }
}