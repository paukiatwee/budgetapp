package io.budgetapp;

import com.bazaarvoice.dropwizard.assets.ConfiguredAssetsBundle;
import io.budgetapp.application.ConstraintViolationExceptionMapper;
import io.budgetapp.application.DataConstraintExceptionMapper;
import io.budgetapp.application.NotFoundExceptionMapper;
import io.budgetapp.application.SQLConstraintViolationExceptionMapper;
import io.budgetapp.auth.TokenAuthProvider;
import io.budgetapp.auth.TokenAuthenticator;
import io.budgetapp.configuration.AppConfiguration;
import io.budgetapp.crypto.PasswordEncoder;
import io.budgetapp.dao.AuthTokenDAO;
import io.budgetapp.dao.CategoryDAO;
import io.budgetapp.dao.LedgerDAO;
import io.budgetapp.dao.LedgerTypeDAO;
import io.budgetapp.dao.RecurringDAO;
import io.budgetapp.dao.TransactionDAO;
import io.budgetapp.dao.UserDAO;
import io.budgetapp.managed.JobsManaged;
import io.budgetapp.managed.MigrationManaged;
import io.budgetapp.model.AuthToken;
import io.budgetapp.model.Category;
import io.budgetapp.model.Ledger;
import io.budgetapp.model.LedgerType;
import io.budgetapp.model.Recurring;
import io.budgetapp.model.Transaction;
import io.budgetapp.model.User;

import com.sun.jersey.api.core.ResourceConfig;
import io.budgetapp.resource.CategoryResource;
import io.budgetapp.resource.LedgerResource;
import io.budgetapp.resource.RecurringResource;
import io.budgetapp.resource.ReportResource;
import io.budgetapp.resource.TransactionResource;
import io.budgetapp.resource.UserResource;
import io.budgetapp.service.FinanceService;
import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class BudgetApplication extends Application<AppConfiguration> {
    public static void main(String[] args) throws Exception {
        new BudgetApplication().run(args);
    }

    private final HibernateBundle<AppConfiguration> hibernate = new HibernateBundle<AppConfiguration>(User.class, Category.class, Ledger.class, LedgerType.class, Transaction.class, Recurring.class, AuthToken.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(AppConfiguration configuration) {
            return configuration.getDataSourceFactory();
        }
    };

    @Override
    public String getName() {
        return "simple-finance";
    }

    @Override
    public void initialize(Bootstrap<AppConfiguration> bootstrap) {
        MigrationsBundle<AppConfiguration> migrationBundle = new MigrationsBundle<AppConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(AppConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        };
        bootstrap.addBundle(migrationBundle);
        bootstrap.addBundle(hibernate);
        bootstrap.addBundle(new ConfiguredAssetsBundle("/app", "/app", "index.html"));
    }

    @Override
    public void run(AppConfiguration configuration, Environment environment) {

        removeUnused(environment);

        // password encoder
        final PasswordEncoder passwordEncoder = new PasswordEncoder();

        // DAO
        final CategoryDAO categoryDAO = new CategoryDAO(hibernate.getSessionFactory(), configuration);
        final LedgerDAO ledgerDAO = new LedgerDAO(hibernate.getSessionFactory(), configuration);
        final LedgerTypeDAO ledgerTypeDAO = new LedgerTypeDAO(hibernate.getSessionFactory());
        final UserDAO userDAO = new UserDAO(hibernate.getSessionFactory());
        final TransactionDAO transactionDAO = new TransactionDAO(hibernate.getSessionFactory());
        final RecurringDAO recurringDAO = new RecurringDAO(hibernate.getSessionFactory());
        final AuthTokenDAO authTokenDAO = new AuthTokenDAO(hibernate.getSessionFactory());

        // service
        final FinanceService financeService = new FinanceService(hibernate.getSessionFactory(), userDAO, ledgerDAO, ledgerTypeDAO, categoryDAO, transactionDAO, recurringDAO, authTokenDAO, passwordEncoder);

        // resource

        environment.jersey().register(new UserResource(financeService));
        environment.jersey().register(new CategoryResource(financeService));
        environment.jersey().register(new LedgerResource(financeService));
        environment.jersey().register(new TransactionResource(financeService));
        environment.jersey().register(new RecurringResource(financeService));
        environment.jersey().register(new ReportResource(financeService));

        // managed
        environment.lifecycle().manage(new MigrationManaged(configuration));
        environment.lifecycle().manage(new JobsManaged(financeService));

        // auth
        environment.jersey().register(new TokenAuthProvider<>(new TokenAuthenticator(financeService)));

        // filters
        FilterRegistration.Dynamic filterRegistration = environment.servlets().addFilter("rewriteFilter", UrlRewriteFilter.class);
        filterRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), false, "/*");
        filterRegistration.setInitParameter("confPath", "urlrewrite.xml");

        // exception mapper
        environment.jersey().register(new NotFoundExceptionMapper());
        environment.jersey().register(new DataConstraintExceptionMapper());
        environment.jersey().register(new ConstraintViolationExceptionMapper());
        environment.jersey().register(new SQLConstraintViolationExceptionMapper());

    }

    private void removeUnused(Environment environment) {
        final ResourceConfig config = environment.jersey().getResourceConfig();
        final Set<Object> singletons = config.getSingletons();
        final List<Object> singletonsToRemove = new ArrayList<>();

        for (Object s : singletons) {
            if (s instanceof io.dropwizard.jersey.validation.ConstraintViolationExceptionMapper) {
                singletonsToRemove.add(s);
            }
        }

        for (Object s : singletonsToRemove) {
            config.getSingletons().remove(s);
        }
    }

}
