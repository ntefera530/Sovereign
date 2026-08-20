package com.sovereign.domain.plaid.service;

import com.plaid.client.model.*;
import com.plaid.client.request.PlaidApi;
import com.sovereign.common.enums.AccountType;
import com.sovereign.common.enums.SyncStatus;
import com.sovereign.common.enums.TransactionType;
import com.sovereign.config.security.UserDetailsImpl;
import com.sovereign.domain.account.entity.Account;
import com.sovereign.domain.account.entity.Transaction;
import com.sovereign.domain.account.repository.AccountRepository;
import com.sovereign.domain.account.repository.TransactionRepository;
import com.sovereign.domain.account.service.AccountService;
import com.sovereign.domain.plaid.dto.request.ExchangePublicTokenRequest;
import com.sovereign.domain.plaid.dto.response.LinkTokenResponse;
import com.sovereign.domain.plaid.dto.response.PlaidItemResponse;
import com.sovereign.domain.plaid.entity.PlaidItem;
import com.sovereign.domain.plaid.repository.PlaidItemRepository;
import com.sovereign.exception.exceptions.BadRequestException;
import com.sovereign.exception.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaidService {

    private final PlaidApi plaidApi;
    private final PlaidItemRepository plaidItemRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public LinkTokenResponse createLinkToken(UserDetailsImpl userDetails) {
        LinkTokenCreateRequest request = new LinkTokenCreateRequest()
            .user(new LinkTokenCreateRequestUser()
                .clientUserId(userDetails.getUser().getId().toString()))
            .clientName("Sovereign")
            .products(List.of(Products.TRANSACTIONS))
            .countryCodes(List.of(CountryCode.US))
            .language("en");

        try {
            Response<LinkTokenCreateResponse> response = plaidApi.linkTokenCreate(request).execute();
            if (!response.isSuccessful() || response.body() == null) {
                log.error("Plaid link token creation failed: {}", errorBody(response));
                throw new BadRequestException("Could not start bank connection");
            }
            return new LinkTokenResponse(response.body().getLinkToken());
        } catch (IOException e) {
            log.error("Plaid link token creation error", e);
            throw new BadRequestException("Could not start bank connection");
        }
    }

    @Transactional
    public List<PlaidItemResponse> exchangePublicToken(
            UserDetailsImpl userDetails, ExchangePublicTokenRequest request) {
        try {
            ItemPublicTokenExchangeRequest exchangeRequest = new ItemPublicTokenExchangeRequest().publicToken(request.publicToken());

            // Exchange the public token for an access token and item ID - returned by Plaid
            Response<ItemPublicTokenExchangeResponse> exchangeResponse = plaidApi.itemPublicTokenExchange(exchangeRequest).execute();
               

            if (!exchangeResponse.isSuccessful() || exchangeResponse.body() == null) {
                log.error("Plaid token exchange failed: {}", errorBody(exchangeResponse));
                throw new BadRequestException("Could not link bank account");
            }

            String accessToken = exchangeResponse.body().getAccessToken();
            String itemId = exchangeResponse.body().getItemId();

            // Save the Plaid item to the database
            PlaidItem plaidItem = new PlaidItem();
            plaidItem.setUser(userDetails.getUser());
            plaidItem.setItemId(itemId);
            plaidItem.setAccessToken(accessToken);
            plaidItem.setInstitutionName(fetchInstitutionName(accessToken));
            plaidItem.setSyncStatus(SyncStatus.SUCCESS);
            plaidItem.setLastSyncedAt(LocalDateTime.now());
            plaidItemRepository.save(plaidItem);

            // Import accounts associated with the Plaid item - it Chase Bank can have (checking, sabings, credit card, etc.)
            importAccounts(userDetails, plaidItem, accessToken);

            log.info("Plaid item linked: {} for user {}", itemId, userDetails.getUser().getId());
            return plaidItemRepository.findByUserId(userDetails.getUser().getId())
                .stream().map(this::toPlaidItemResponse).toList();

        } catch (IOException e) {
            log.error("Plaid token exchange error", e);
            throw new BadRequestException("Could not link bank account");
        }
    }

    @Transactional
    public void syncTransactions(UserDetailsImpl userDetails, UUID plaidItemId) {
        PlaidItem plaidItem = findItemForUser(plaidItemId, userDetails.getUser().getId());

        try {
            String cursor = plaidItem.getSyncCursor();
            boolean hasMore = true;

            while (hasMore) {
                TransactionsSyncRequest syncRequest = new TransactionsSyncRequest()
                    .accessToken(plaidItem.getAccessToken())
                    .cursor(cursor);

                Response<TransactionsSyncResponse> response = plaidApi.transactionsSync(syncRequest).execute();
                if (!response.isSuccessful() || response.body() == null) {
                    plaidItem.setSyncStatus(SyncStatus.FAILED);
                    plaidItemRepository.save(plaidItem);
                    log.error("Plaid sync failed for item {}: {}", plaidItem.getId(), errorBody(response));
                    throw new BadRequestException("Could not sync transactions");
                }

                TransactionsSyncResponse body = response.body();
                body.getAdded().forEach(this::upsertTransaction);
                body.getModified().forEach(this::upsertTransaction);
                body.getRemoved().forEach(t ->
                    transactionRepository.findByFingerprint("plaid:" + t.getTransactionId())
                        .ifPresent(transactionRepository::delete));

                cursor = body.getNextCursor();
                hasMore = body.getHasMore();
            }

            plaidItem.setSyncCursor(cursor);
            plaidItem.setSyncStatus(SyncStatus.SUCCESS);
            plaidItem.setLastSyncedAt(LocalDateTime.now());
            plaidItemRepository.save(plaidItem);

        } catch (IOException e) {
            plaidItem.setSyncStatus(SyncStatus.FAILED);
            plaidItemRepository.save(plaidItem);
            log.error("Plaid sync error for item {}", plaidItem.getId(), e);
            throw new BadRequestException("Could not sync transactions");
        }
    }

    @Transactional
    public void removeItem(UserDetailsImpl userDetails, UUID plaidItemId) {
        PlaidItem plaidItem = findItemForUser(plaidItemId, userDetails.getUser().getId());
        try {
            plaidApi.itemRemove(new ItemRemoveRequest().accessToken(plaidItem.getAccessToken())).execute();
        } catch (IOException e) {
            log.warn("Plaid item removal call failed, deleting locally anyway", e);
        }
        plaidItemRepository.delete(plaidItem);
        log.info("Plaid item removed: {}", plaidItemId);
    }

    public List<PlaidItemResponse> getItems(UserDetailsImpl userDetails) {
        return plaidItemRepository.findByUserId(userDetails.getUser().getId())
            .stream().map(this::toPlaidItemResponse).toList();
    }















    // ── Helpers ──────────────────────────────────────────────

    private void importAccounts(UserDetailsImpl userDetails, PlaidItem plaidItem, String accessToken)
            throws IOException {
        Response<AccountsGetResponse> accountsResponse =
            plaidApi.accountsGet(new AccountsGetRequest().accessToken(accessToken)).execute();

        if (!accountsResponse.isSuccessful() || accountsResponse.body() == null) {
            throw new BadRequestException("Could not fetch accounts from bank");
        }

        //TODO: Need to differentiate between accounts (checking, savings, etc)debts (credic card, loan, etc) and investments (stocks, crypto, etc)
        for (AccountBase plaidAccount : accountsResponse.body().getAccounts()) {
            if (accountRepository.findByPlaidAccountId(plaidAccount.getAccountId()).isPresent()) {
                continue; // already imported
            }

            // if(plaidAccount.getType().CREDIT == "credit" || plaidAccount.getType() == "loan") {
            //     continue; // skip credit and loan accounts for now
            // }
            Account account = new Account();
            account.setUser(userDetails.getUser());
            account.setName(plaidAccount.getName());
            account.setType(mapAccountType(plaidAccount.getType().getValue()));
            account.setBalance(BigDecimal.valueOf(
                plaidAccount.getBalances().getCurrent() != null
                    ? plaidAccount.getBalances().getCurrent() : 0));
            account.setCurrency(plaidAccount.getBalances().getIsoCurrencyCode() != null
                ? plaidAccount.getBalances().getIsoCurrencyCode() : "USD");
            account.setPlaidAccountId(plaidAccount.getAccountId());
            account.setPlaidItem(plaidItem);
            accountRepository.save(account);
        }
    }

    private void upsertTransaction(com.plaid.client.model.Transaction plaidTx) {
        Account account = accountRepository.findByPlaidAccountId(plaidTx.getAccountId()).orElse(null);
        if (account == null) return;

        String fingerprint = "plaid:" + plaidTx.getTransactionId();
        Transaction transaction = transactionRepository.findByFingerprint(fingerprint)
            .orElseGet(Transaction::new);

        transaction.setAccount(account);
        transaction.setAmount(BigDecimal.valueOf(Math.abs(plaidTx.getAmount())));
        // Plaid convention: positive amount = money leaving the account (expense)
        transaction.setType(plaidTx.getAmount() != null && plaidTx.getAmount() > 0
            ? TransactionType.EXPENSE : TransactionType.INCOME);
        transaction.setDescription(plaidTx.getMerchantName() != null
            ? plaidTx.getMerchantName() : plaidTx.getName());
        transaction.setTransactionDate(plaidTx.getDate());
        transaction.setFingerprint(fingerprint);

        if (transaction.getId() == null) {
            accountService.importTransaction(account, transaction); // keeps balance in sync
        } else {
            transactionRepository.save(transaction);
        }
    }

    private String fetchInstitutionName(String accessToken) throws IOException {
        Response<ItemGetResponse> itemResponse =
            plaidApi.itemGet(new ItemGetRequest().accessToken(accessToken)).execute();
        if (!itemResponse.isSuccessful() || itemResponse.body() == null) return "Unknown institution";

        String institutionId = itemResponse.body().getItem().getInstitutionId();
        if (institutionId == null) return "Unknown institution";

        Response<InstitutionsGetByIdResponse> instResponse = plaidApi.institutionsGetById(
            new InstitutionsGetByIdRequest()
                .institutionId(institutionId)
                .countryCodes(List.of(CountryCode.US))
        ).execute();

        return instResponse.isSuccessful() && instResponse.body() != null
            ? instResponse.body().getInstitution().getName()
            : "Unknown institution";
    }

    private AccountType mapAccountType(String plaidType) {
        return switch (plaidType) {
            case "depository" -> AccountType.CHECKING;
            case "credit" -> AccountType.CREDIT_CARD;
            case "loan" -> AccountType.LOAN;
            case "investment" -> AccountType.INVESTMENT;
            default -> AccountType.OTHER;
        };
    }

    private PlaidItem findItemForUser(UUID plaidItemId, UUID userId) {
        PlaidItem item = plaidItemRepository.findById(plaidItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Bank connection not found"));
        if (!item.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Bank connection not found");
        }
        return item;
    }

    private PlaidItemResponse toPlaidItemResponse(PlaidItem item) {
        int accountCount = (int) accountRepository.findByUserId(item.getUser().getId()).stream()
            .filter(a -> a.getPlaidItem() != null && a.getPlaidItem().getId().equals(item.getId()))
            .count();
        return new PlaidItemResponse(
            item.getId(), item.getInstitutionName(), item.getSyncStatus(),
            item.getLastSyncedAt(), accountCount
        );
    }

    private String errorBody(Response<?> response) {
        try {
            return response.errorBody() != null ? response.errorBody().string() : "no body";
        } catch (IOException e) {
            return "unreadable error body";
        }
    }
}