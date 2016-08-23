package io.budgetapp.managed;

import com.codahale.metrics.MetricRegistry;
import io.budgetapp.configuration.AppConfiguration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.migrations.CloseableLiquibase;
import io.dropwizard.migrations.CloseableLiquibaseWithClassPathMigrationsFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class MigrationManaged implements Managed {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationManaged.class);


    private final DataSourceFactory dataSourceFactory;

    public MigrationManaged(AppConfiguration configuration) {
        this.dataSourceFactory = configuration.getDataSourceFactory();
    }

    @Override
    public void start() throws Exception {
        LOGGER.info("begin migration");
        final ManagedDataSource dataSource = dataSourceFactory.build(new MetricRegistry(), "liquibase");
        try(CloseableLiquibase liquibase = new CloseableLiquibaseWithClassPathMigrationsFile(dataSource, "migrations.xml")) {
            liquibase.update("migrations");
        }
        LOGGER.info("finish migration");
    }

    @Override
    public void stop() throws Exception {

    }
}
