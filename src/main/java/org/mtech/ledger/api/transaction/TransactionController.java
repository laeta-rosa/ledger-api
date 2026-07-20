package org.mtech.ledger.api.transaction;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.mtech.ledger.domain.transaction.TransactionType;
import org.mtech.ledger.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts/{id}")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Deposit, withdraw, and view transaction history")
public class TransactionController {

    private final TransactionService transactions;

    @GetMapping("/transactions")
    @Swagger.GetHistory.Description
    public List<TransactionResponse> getHistory(@PathVariable UUID id) {
        return transactions.getHistory(id).stream().map(TransactionResponse::from).toList();
    }

    @PostMapping("/deposit")
    @ResponseStatus(HttpStatus.CREATED)
    @Swagger.Deposit.Description
    public TransactionResponse deposit(
            @PathVariable UUID id, @Valid @RequestBody CreateTransactionRequest request) {
        var transaction = transactions.record(id, TransactionType.DEPOSIT, request.amount());
        return TransactionResponse.from(transaction);
    }

    @PostMapping("/withdrawal")
    @ResponseStatus(HttpStatus.CREATED)
    @Swagger.Withdraw.Description
    public TransactionResponse withdraw(
            @PathVariable UUID id, @Valid @RequestBody CreateTransactionRequest request) {
        var transaction = transactions.record(id, TransactionType.WITHDRAWAL, request.amount());
        return TransactionResponse.from(transaction);
    }
}