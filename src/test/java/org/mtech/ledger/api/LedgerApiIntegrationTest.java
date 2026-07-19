package org.mtech.ledger.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class LedgerApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String createAccount() throws Exception {
        String body = mockMvc.perform(post("/accounts"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balance").value(0.00))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    @Test
    void fullDepositWithdrawFlow() throws Exception {
        String accountId = createAccount();

        mockMvc.perform(post("/accounts/{id}/transactions", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"DEPOSIT\",\"amount\":100.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balanceAfter").value(100.00));

        mockMvc.perform(post("/accounts/{id}/transactions", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"WITHDRAWAL\",\"amount\":30.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balanceAfter").value(70.00));

        mockMvc.perform(get("/accounts/{id}/balance", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(70.00));

        mockMvc.perform(get("/accounts/{id}/transactions", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("WITHDRAWAL"))
                .andExpect(jsonPath("$[1].type").value("DEPOSIT"));
    }

    @Test
    void unknownAccountReturns404() throws Exception {
        mockMvc.perform(get("/accounts/{id}/balance", "00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void overdraftReturns422() throws Exception {
        String accountId = createAccount();

        mockMvc.perform(post("/accounts/{id}/transactions", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"WITHDRAWAL\",\"amount\":1.00}"))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void invalidAmountReturns400() throws Exception {
        String accountId = createAccount();

        mockMvc.perform(post("/accounts/{id}/transactions", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"DEPOSIT\",\"amount\":-5.00}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidTypeReturns400() throws Exception {
        String accountId = createAccount();

        mockMvc.perform(post("/accounts/{id}/transactions", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"TRANSFER\",\"amount\":5.00}"))
                .andExpect(status().isBadRequest());
    }
}
