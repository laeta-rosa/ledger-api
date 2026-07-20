package org.mtech.ledger.api;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.mtech.ledger.domain.Transaction;
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
@RequestMapping("/accounts/{id}/transactions")
public class TransactionController {

    private final TransactionService transactions;

    public TransactionController(TransactionService transactions) {
        this.transactions = transactions;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse recordTransaction(
            @PathVariable UUID id, @Valid @RequestBody CreateTransactionRequest request) {
        Transaction transaction = transactions.record(id, request.type(), request.amount());
        return TransactionResponse.from(transaction);
    }

    @GetMapping
    public List<TransactionResponse> getHistory(@PathVariable UUID id) {
        return transactions.getHistory(id).stream().map(TransactionResponse::from).toList();
    }
}