package org.mtech.ledger.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.mtech.ledger.domain.Account;
import org.mtech.ledger.domain.Transaction;
import org.mtech.ledger.service.LedgerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final LedgerService ledger;

    public AccountController(LedgerService ledger) {
        this.ledger = ledger;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount() {
        Account account = ledger.createAccount();
        return new AccountResponse(account.getId(), account.getBalance());
    }

    @GetMapping("/{id}/balance")
    public AccountResponse getBalance(@PathVariable UUID id) {
        return new AccountResponse(id, ledger.getBalance(id));
    }

    @PostMapping("/{id}/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction recordTransaction(
            @PathVariable UUID id, @Valid @RequestBody CreateTransactionRequest request) {
        return ledger.record(id, request.type(), request.amount());
    }

    @GetMapping("/{id}/transactions")
    public List<Transaction> getHistory(@PathVariable UUID id) {
        return ledger.getHistory(id);
    }
}
