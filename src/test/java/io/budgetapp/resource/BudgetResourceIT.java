package io.budgetapp.resource;

import com.sun.jersey.api.client.ClientResponse;
import io.budgetapp.BudgetApplication;
import io.budgetapp.configuration.AppConfiguration;
import io.budgetapp.modal.IdentityResponse;
import io.budgetapp.model.Budget;
import io.budgetapp.model.form.TransactionForm;
import io.budgetapp.model.form.budget.AddBudgetForm;
import io.budgetapp.model.form.budget.UpdateBudgetForm;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;

/**
 *
 */
public class BudgetResourceIT extends ResourceIT {

    @ClassRule
    public static final DropwizardAppRule<AppConfiguration> RULE =
            new DropwizardAppRule<>(BudgetApplication.class, resourceFilePath("config-test.yml"));

    @Override
    protected int getPort() {
        return RULE.getLocalPort();
    }

    @Test
    public void shouldAbleToListBudgets() {

        // given user (created from ResourceIT)

        // when
        ClientResponse response = get("/api/budgets");
        assertOk(response);
        List<IdentityResponse> identityResponses = identityResponses(response);

        // then
        Assert.assertTrue(identityResponses.size() >= 0);
    }

    @Test
    public void shouldAbleCreateBudget() {

        // given
        AddBudgetForm budget = new AddBudgetForm();
        budget.setName(randomAlphabets());
        budget.setCategoryId(defaultCategory.getId());

        // when
        ClientResponse response = post("/api/budgets", budget);

        // then
        assertCreated(response);
        Assert.assertNotNull(response.getLocation());
    }

    @Test
    public void shouldBeAbleUpdateBudget() {
        // given
        AddBudgetForm budget = new AddBudgetForm();
        budget.setName(randomAlphabets());
        budget.setCategoryId(defaultCategory.getId());
        ClientResponse createdResponse = post("/api/budgets", budget);
        long budgetId = identityResponse(createdResponse).getId();

        // when
        UpdateBudgetForm updateBudgetForm = new UpdateBudgetForm();
        updateBudgetForm.setId(budgetId);
        updateBudgetForm.setName("Test");
        updateBudgetForm.setProjected(100);
        ClientResponse updateResponse = put("/api/budgets/" + budgetId, updateBudgetForm);
        Budget updatedBudget = updateResponse.getEntity(Budget.class);

        // then
        assertCreated(createdResponse);
        Assert.assertNotNull(createdResponse.getLocation());
        Assert.assertEquals("Test", updatedBudget.getName());
        Assert.assertEquals(100, updatedBudget.getProjected(), 0.000);
    }

    @Test
    public void shouldAbleFindValidBudget() {

        // given
        AddBudgetForm budget = new AddBudgetForm();
        budget.setName(randomAlphabets());
        budget.setCategoryId(1L);

        // when
        ClientResponse response = post("/api/budgets", budget);

        // then
        ClientResponse newReponse = get(response.getLocation().getPath());
        assertOk(newReponse);
    }

    @Test
    public void shouldNotAbleDeleteBudgetWithChild() {

        // given
        AddBudgetForm addBudgetForm = new AddBudgetForm();
        addBudgetForm.setName(randomAlphabets());
        addBudgetForm.setCategoryId(1L);

        // when
        ClientResponse response = post("/api/budgets", addBudgetForm);
        TransactionForm transactionForm = new TransactionForm();
        transactionForm.setAmount(10.00);
        Budget budget = new Budget();
        budget.setId(identityResponse(response).getId());
        transactionForm.setBudget(budget);
        post("/api/transactions", transactionForm);

        // then
        ClientResponse newReponse = delete(response.getLocation().getPath());
        assertBadRequest(newReponse);
    }

}
