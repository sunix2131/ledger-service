package dev.sunix.ledger.api;

import static dev.sunix.ledger.api.LedgerApiModels.BalanceResponse;
import static dev.sunix.ledger.api.LedgerApiModels.CreateAccountRequest;
import static dev.sunix.ledger.api.LedgerApiModels.CreateTransactionRequest;
import static dev.sunix.ledger.api.LedgerApiModels.PostingPage;
import static dev.sunix.ledger.api.LedgerApiModels.ReverseTransactionRequest;
import static dev.sunix.ledger.api.LedgerApiModels.TransactionResponse;

import dev.sunix.ledger.domain.Account;
import dev.sunix.ledger.service.AccountService;
import dev.sunix.ledger.service.LedgerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1")
public class LedgerController {
    private final AccountService accounts;
    private final LedgerService ledger;

    public LedgerController(AccountService accounts, LedgerService ledger) {
        this.accounts = accounts;
        this.ledger = ledger;
    }

    @PostMapping("/accounts")
    ResponseEntity<Account> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Account account = accounts.create(
                request.name(), request.currency(), request.normalSide(), request.allowNegative());
        return ResponseEntity.created(URI.create("/api/v1/accounts/" + account.id())).body(account);
    }

    @GetMapping("/accounts/{id}")
    Account account(@PathVariable UUID id) {
        return accounts.get(id);
    }

    @GetMapping("/accounts/{id}/balance")
    BalanceResponse balance(@PathVariable UUID id) {
        Account account = accounts.get(id);
        return new BalanceResponse(id, account.currency(), accounts.balance(id));
    }

    @GetMapping("/accounts/{id}/postings")
    PostingPage postings(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int limit,
            @RequestParam(defaultValue = "0") @Min(0) int offset) {
        return new PostingPage(accounts.postings(id, limit, offset), limit, offset);
    }

    @PostMapping("/transactions")
    ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestHeader(value = "X-Actor", required = false) String actor,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        var result = ledger.create(request, idempotencyKey, actor, requestId);
        var response = TransactionResponse.from(result.transaction(), result.replay());
        return result.replay()
                ? ResponseEntity.ok(response)
                : ResponseEntity.created(URI.create("/api/v1/transactions/" + response.id())).body(response);
    }

    @GetMapping("/transactions/{id}")
    TransactionResponse transaction(@PathVariable UUID id) {
        return TransactionResponse.from(ledger.get(id), false);
    }

    @PostMapping("/transactions/{id}/reverse")
    ResponseEntity<TransactionResponse> reverse(
            @PathVariable UUID id,
            @Valid @RequestBody ReverseTransactionRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestHeader(value = "X-Actor", required = false) String actor,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        var result = ledger.reverse(id, request.reason(), idempotencyKey, actor, requestId);
        var response = TransactionResponse.from(result.transaction(), result.replay());
        return result.replay()
                ? ResponseEntity.ok(response)
                : ResponseEntity.created(URI.create("/api/v1/transactions/" + response.id())).body(response);
    }
}
