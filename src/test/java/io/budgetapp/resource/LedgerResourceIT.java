package io.budgetapp.resource;

import com.sun.jersey.api.client.ClientResponse;
import io.budgetapp.BudgetApplication;
import io.budgetapp.configuration.AppConfiguration;
import io.budgetapp.modal.IdentityResponse;
import io.budgetapp.model.form.ledger.AddLedgerForm;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;

/**
 *
 */
public class LedgerResourceIT extends ResourceIT {

    @ClassRule
    public static final DropwizardAppRule<AppConfiguration> RULE =
            new DropwizardAppRule<>(BudgetApplication.class, resourceFilePath("config-test.yml"));

    @Override
    protected int getPort() {
        return RULE.getLocalPort();
    }

    @Test
    public void shouldAbleToListLedgers() {

        // give user (created from ResourceIT)

        // when
        ClientResponse response = get("/api/ledgers");
        assertOk(response);
        List<IdentityResponse> identityResponses = identityResponses(response);

        // then
        Assert.assertTrue(identityResponses.size() >= 0);
    }

    @Test
    public void shouldAbleCreateLedger() {

        // give
        AddLedgerForm ledger = new AddLedgerForm();
        ledger.setName(randomAlphabets());
        ledger.setCategoryId(defaultCategory.getId());

        // when
        ClientResponse response = post("/api/ledgers", ledger);

        // then
        assertCreated(response);
        Assert.assertNotNull(response.getLocation());
    }

    @Test
    public void shouldAbleFindValidLedger() {

        // give
        AddLedgerForm ledger = new AddLedgerForm();
        ledger.setName(randomAlphabets());
        ledger.setCategoryId(1L);

        // when
        ClientResponse response = post("/api/ledgers", ledger);

        // then
        ClientResponse newReponse = get(response.getLocation().getPath());
        assertOk(newReponse);
    }

}
