package io.budgetapp.resource;

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

import javax.ws.rs.core.Response;
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
        Response response = get(ResourceURL.BUDGET);
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
        Response response = post(ResourceURL.BUDGET, budget);

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
        Response createdResponse = post(ResourceURL.BUDGET, budget);
        long budgetId = identityResponse(createdResponse).getId();

        // when
        UpdateBudgetForm updateBudgetForm = new UpdateBudgetForm();
        updateBudgetForm.setId(budgetId);
        updateBudgetForm.setName("Test");
        updateBudgetForm.setProjected(100);
        Response updateResponse = put("/api/budgets/" + budgetId, updateBudgetForm);
        Budget updatedBudget = updateResponse.readEntity(Budget.class);

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
        Response response = post(ResourceURL.BUDGET, budget);

        // then
        Response newReponse = get(response.getLocation().getPath());
        assertOk(newReponse);
    }

    @Test
    public void shouldNotAbleDeleteBudgetWithChild() {

        // given
        AddBudgetForm addBudgetForm = new AddBudgetForm();
        addBudgetForm.setName(randomAlphabets());
        addBudgetForm.setCategoryId(1L);

        // when
        Response response = post(ResourceURL.BUDGET, addBudgetForm);
        TransactionForm transactionForm = new TransactionForm();
        transactionForm.setAmount(10.00);
        Budget budget = new Budget();
        budget.setId(identityResponse(response).getId());
        transactionForm.setBudget(budget);
        post(ResourceURL.TRANSACTION, transactionForm);

        // then
        Response newReponse = delete(response.getLocation().getPath());
        assertBadRequest(newReponse);
    }

}
