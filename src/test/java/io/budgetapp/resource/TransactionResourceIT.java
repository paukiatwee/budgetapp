package io.budgetapp.resource;


import io.budgetapp.BudgetApplication;
import io.budgetapp.configuration.AppConfiguration;
import io.budgetapp.modal.IdentityResponse;
import io.budgetapp.model.Budget;
import io.budgetapp.model.Category;
import io.budgetapp.model.CategoryType;
import io.budgetapp.model.form.TransactionForm;
import io.budgetapp.model.form.budget.AddBudgetForm;
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
        transaction.setBudget(defaultBudget);

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
        transaction.setBudget(defaultBudget);

        // when
        ClientResponse response = post("/api/transactions", transaction);

        // then
        ClientResponse newReponse = get(response.getLocation().getPath());
        assertOk(newReponse);
    }

    @Test
    public void shouldAbleFindTransactionsByBudget() {
        // give
        Category category = new Category();
        category.setName(randomAlphabets());
        category.setType(CategoryType.EXPENSE);

        AddBudgetForm budget = new AddBudgetForm();
        budget.setName(randomAlphabets());

        TransactionForm transaction = new TransactionForm();

        // when
        ClientResponse categoryResponse = post("/api/categories", category);
        Long categoryId = identityResponse(categoryResponse).getId();
        budget.setCategoryId(categoryId);

        ClientResponse budgetResponse = post("/api/budgets", budget);
        Long budgetId = identityResponse(budgetResponse).getId();

        transaction.setBudget(new Budget(budgetId));
        post("/api/transactions", transaction);

        // then
        ClientResponse newResponse = get("/api/budgets/" + budgetId + "/transactions");
        List<IdentityResponse> ids = identityResponses(newResponse);
        assertOk(newResponse);
        Assert.assertEquals(1, ids.size());
    }
}
