package io.budgetapp.resource;

import io.budgetapp.BudgetApplication;
import io.budgetapp.configuration.AppConfiguration;
import io.budgetapp.model.RecurringType;
import io.budgetapp.model.form.TransactionForm;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.Response;

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
        Response before = get(ResourceURL.RECURRING);
        int originalCount = identityResponses(before).size();

        // given
        TransactionForm transaction = new TransactionForm();
        transaction.setAmount(10.00);
        transaction.setRecurring(Boolean.TRUE);
        transaction.setRecurringType(RecurringType.MONTHLY);
        transaction.setBudget(defaultBudget);

        // when
        Response response = post(ResourceURL.TRANSACTION, transaction);

        // then
        assertCreated(response);
        Assert.assertNotNull(response.getLocation());

        Response after = get(ResourceURL.RECURRING);
        int finalCount = identityResponses(after).size();
        Assert.assertTrue(finalCount - originalCount - 1 == 0);
    }
}
