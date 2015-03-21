package io.budgetapp.resource;


import io.budgetapp.BudgetApplication;
import io.budgetapp.configuration.AppConfiguration;
import io.budgetapp.model.Category;
import io.budgetapp.model.CategoryType;
import io.budgetapp.model.form.budget.AddBudgetForm;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.Response;


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

        // given
        Category category = new Category();
        category.setName(randomAlphabets());
        category.setType(CategoryType.EXPENDITURE);

        // when
        Response response = post(ResourceURL.CATEGORY, category);

        // then
        assertCreated(response);
        Assert.assertNotNull(response.getLocation());
    }

    @Test
    public void shouldAbleFindValidCategory() {

        // given
        Category category = new Category();
        category.setName(randomAlphabets());
        category.setType(CategoryType.EXPENDITURE);

        // when
        Response response = post(ResourceURL.CATEGORY, category);

        // then
        Response newReponse = get(response.getLocation().getPath());
        assertOk(newReponse);
    }


    @Test
    public void shouldNotAbleDeleteCategoryWithChild() {

        // given
        Category category = new Category();
        category.setName(randomAlphabets());
        category.setType(CategoryType.EXPENDITURE);

        // when
        Response response = post(ResourceURL.CATEGORY, category);
        AddBudgetForm budget = new AddBudgetForm();
        budget.setName(randomAlphabets());
        budget.setCategoryId(identityResponse(response).getId());
        post(ResourceURL.BUDGET, budget);

        // then
        Response newReponse = delete(response.getLocation().getPath());
        assertBadRequest(newReponse);
    }
}
