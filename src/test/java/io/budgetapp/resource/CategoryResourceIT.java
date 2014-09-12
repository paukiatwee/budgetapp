package io.budgetapp.resource;


import com.sun.jersey.api.client.ClientResponse;
import io.budgetapp.BudgetApplication;
import io.budgetapp.configuration.AppConfiguration;
import io.budgetapp.model.Category;
import io.budgetapp.model.CategoryType;
import io.budgetapp.model.form.ledger.AddLedgerForm;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;


/**
 *
 */
public class CategoryResourceIT extends ResourceIT {

    @ClassRule
    public static final DropwizardAppRule<AppConfiguration> RULE =
            new DropwizardAppRule<>(BudgetApplication.class, resourceFilePath("config-test.yml"));

    @Override
    protected int getPort() {
        return RULE.getLocalPort();
    }

    @Test
    public void shouldAbleCreateCategory() {

        // give
        Category category = new Category();
        category.setName(randomAlphabets());
        category.setType(CategoryType.EXPENSE);

        // when
        ClientResponse response = post("/api/categories", category);

        // then
        assertCreated(response);
        Assert.assertNotNull(response.getLocation());
    }

    @Test
    public void shouldAbleFindValidCategory() {

        // give
        Category category = new Category();
        category.setName(randomAlphabets());
        category.setType(CategoryType.EXPENSE);

        // when
        ClientResponse response = post("/api/categories", category);

        // then
        ClientResponse newReponse = get(response.getLocation().getPath());
        assertOk(newReponse);
    }


    @Test
    public void shouldNotAbleDeleteCategoryWithChild() {

        // give
        Category category = new Category();
        category.setName(randomAlphabets());
        category.setType(CategoryType.EXPENSE);

        // when
        ClientResponse response = post("/api/categories", category);
        AddLedgerForm ledger = new AddLedgerForm();
        ledger.setName(randomAlphabets());
        ledger.setCategoryId(identityResponse(response).getId());
        post("/api/ledgers", ledger);

        // then
        ClientResponse newReponse = delete(response.getLocation().getPath());
        assertBadRequest(newReponse);
    }
}
