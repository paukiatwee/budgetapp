package io.budgetapp.configuration;

import com.bazaarvoice.dropwizard.assets.AssetsBundleConfiguration;
import com.bazaarvoice.dropwizard.assets.AssetsConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.budgetapp.model.Budget;
import io.budgetapp.model.Category;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class AppConfiguration extends Configuration implements AssetsBundleConfiguration {

    @Valid
    @NotNull
    @JsonProperty("database")
    private DataSourceFactory database = new DataSourceFactory();

    @Valid
    @NotNull
    @JsonProperty
    private final AssetsConfiguration assets = new AssetsConfiguration();

    @Valid
    @NotNull
    @JsonProperty("categories")
    private List<Category> categories = new ArrayList<>();

    @Valid
    @NotNull
    @JsonProperty("budgets")
    private Map<String, List<Budget>> budgets = new LinkedHashMap<>();

    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @Override
    public AssetsConfiguration getAssetsConfiguration() {
        return assets;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public Map<String, List<Budget>> getBudgets() {
        return budgets;
    }
}