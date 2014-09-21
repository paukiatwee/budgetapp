package io.budgetapp.resource;

import com.sun.jersey.api.client.ClientResponse;
import io.budgetapp.BudgetApplication;
import io.budgetapp.configuration.AppConfiguration;
import io.budgetapp.model.RecurringType;
import io.budgetapp.model.form.TransactionForm;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

/**
 *
 */
public class RecurringResourceIT extends ResourceIT {


    @ClassRule
    public static final DropwizardAppRule<AppConfiguration> RULE =
            new DropwizardAppRule<>(BudgetApplication.class, resourceFilePath("config-test.yml"));

    @Override
    protected int getPort() {
        return RULE.getLocalPort();
    }


    @Test
    public void shouldAbleCreateRecurring() {

        // before
        ClientResponse before = get("/api/recurrings");
        int originalCount = identityResponses(before).size();

        // give
        TransactionForm transaction = new TransactionForm();
        transaction.setRecurring(Boolean.TRUE);
        transaction.setRecurringType(RecurringType.MONTHLY);
        transaction.setBudget(defaultBudget);

        // when
        ClientResponse response = post("/api/transactions", transaction);

        // then
        assertCreated(response);
        Assert.assertNotNull(response.getLocation());

        ClientResponse after = get("/api/recurrings");
        int finalCount = identityResponses(after).size();
        Assert.assertTrue(finalCount - originalCount - 1 == 0);
    }
}
