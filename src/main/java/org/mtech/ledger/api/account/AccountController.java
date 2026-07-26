package org.mtech.ledger.api.account;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.mtech.ledger.domain.vo.AccountId;
import org.mtech.ledger.service.AccountService;
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
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Create accounts and query their balance")
public class AccountController {

    private final AccountService accounts;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Swagger.CreateAccount.Description
    public AccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request) {
        var account = accounts.createAccount(request.name(), request.surname());
        return AccountResponse.from(account);
    }

    @GetMapping("/{id}/balance")
    @Swagger.GetBalance.Description
    public AccountResponse getBalance(@PathVariable UUID id) {
        var account = accounts.getAccount(AccountId.of(id));
        return AccountResponse.from(account);
    }
}