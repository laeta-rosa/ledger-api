package org.mtech.ledger.adapter.inbound.rest.account;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.mtech.ledger.adapter.inbound.rest.account.api.AccountResponse;
import org.mtech.ledger.adapter.inbound.rest.account.api.CreateAccountRequest;
import org.mtech.ledger.application.account.create.CreateAccountCommand;
import org.mtech.ledger.application.account.create.CreateAccountUseCase;
import org.mtech.ledger.application.account.balancequery.AccountBalanceQuery;
import org.mtech.ledger.application.account.balancequery.AccountBalanceQueryUseCase;
import org.mtech.ledger.application.account.AccountResult.NotFound;
import org.mtech.ledger.application.account.AccountResult.Success;
import org.mtech.ledger.domain.account.AccountNotFoundException;
import org.mtech.ledger.domain.vo.AccountId;
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

    private final CreateAccountUseCase createAccount;
    private final AccountBalanceQueryUseCase getAccountBalance;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Swagger.CreateAccount.Description
    public AccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request) {
        var result = createAccount.invoke(new CreateAccountCommand(request.name(), request.surname()));
        return AccountResponse.from(result);
    }

    @GetMapping("/{id}/balance")
    @Swagger.GetBalance.Description
    public AccountResponse getBalance(@PathVariable UUID id) {
        var result = getAccountBalance.invoke(new AccountBalanceQuery(AccountId.of(id)));
        return switch (result) {
            case Success found -> AccountResponse.from(found);
            case NotFound notFound -> throw new AccountNotFoundException(notFound.id());
        };
    }
}
