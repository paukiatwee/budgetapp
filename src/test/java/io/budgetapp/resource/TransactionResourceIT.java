package io.budgetapp.resource;


import io.budgetapp.BudgetApplication;
import io.budgetapp.configuration.AppConfiguration;
import io.budgetapp.modal.IdentityResponse;
import io.budgetapp.model.Category;
import io.budgetapp.model.CategoryType;
import io.budgetapp.model.Ledger;
import io.budgetapp.model.form.TransactionForm;
import io.budgetapp.model.form.ledger.AddLedgerForm;
import com.sun.jersey.api.client.ClientResponse;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;


/**
 *
 */
public class TransactionResourceIT extends ResourceIT {

    @ClassRule
    public static final DropwizardAppRule<AppConfiguration> RULE =
            new DropwizardAppRule<>(BudgetApplication.class, resourceFilePath("config-test.yml"));

    @Override
    protected int getPort() {
        return RULE.getLocalPort();
    }

    @Test
    public void shouldAbleCreateTransaction() {

        // give
        TransactionForm transaction = new TransactionForm();
        transaction.setLedger(defaultLedger);

        // when
        ClientResponse response = post("/api/transactions", transaction);

        // then
        assertCreated(response);
        Assert.assertNotNull(response.getLocation());
    }

    @Test
    public void shouldAbleFindValidTransaction() {

        // give
        TransactionForm transaction = new TransactionForm();
        transaction.setLedger(defaultLedger);

        // when
        ClientResponse response = post("/api/transactions", transaction);

        // then
        ClientResponse newReponse = get(response.getLocation().getPath());
        assertOk(newReponse);
    }

    @Test
    public void shouldAbleFindTransactionsByLedger() {
        // give
        Category category = new Category();
        category.setName(randomAlphabets());
        category.setType(CategoryType.EXPENSE);

        AddLedgerForm ledger = new AddLedgerForm();
        ledger.setName(randomAlphabets());

        TransactionForm transaction = new TransactionForm();

        // when
        ClientResponse categoryResponse = post("/api/categories", category);
        Long categoryId = identityResponse(categoryResponse).getId();
        ledger.setCategoryId(categoryId);

        ClientResponse ledgerResponse = post("/api/ledgers", ledger);
        Long ledgerId = identityResponse(ledgerResponse).getId();

        transaction.setLedger(new Ledger(ledgerId));
        post("/api/transactions", transaction);

        // then
        ClientResponse newResponse = get("/api/ledgers/" + ledgerId + "/transactions");
        List<IdentityResponse> ids = identityResponses(newResponse);
        assertOk(newResponse);
        Assert.assertEquals(1, ids.size());
    }
}
