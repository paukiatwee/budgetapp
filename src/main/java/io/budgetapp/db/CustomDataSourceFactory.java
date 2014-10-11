package io.budgetapp.db;

import io.budgetapp.util.Util;
import io.dropwizard.db.DataSourceFactory;

import java.net.URI;
import java.util.Optional;

/**
 *
 */
public class CustomDataSourceFactory extends DataSourceFactory {

    private Optional<String> cloudDatabaseUrl = Optional.empty();

    public CustomDataSourceFactory() {
        cloudDatabaseUrl = System
                .getenv().keySet()
                .stream().filter(e -> e.startsWith("HEROKU_POSTGRESQL_"))
                .findFirst();
    }

    @Override
    public void setUser(String user) {
        if(cloudDatabaseUrl.isPresent()) {
            super.setUser(Util.getDatabaseURL(cloudDatabaseUrl.get()).getUserInfo().split(":")[0]);
        } else {
            super.setUser(user);
        }
    }

    @Override
    public void setPassword(String password) {
        if(cloudDatabaseUrl.isPresent()) {
            super.setPassword(Util.getDatabaseURL(cloudDatabaseUrl.get()).getUserInfo().split(":")[1]);
        } else {
            super.setPassword(password);
        }
    }

    @Override
    public void setUrl(String url) {
        if (cloudDatabaseUrl.isPresent()) {
            URI uri = Util.getDatabaseURL(cloudDatabaseUrl.get());
            super.setUrl("jdbc:postgresql://" + uri.getHost() + ':' + uri.getPort() + uri.getPath());
        } else {
            super.setUrl(url);
        }
    }

    @Override
    public void setDriverClass(String driverClass) {
        if (cloudDatabaseUrl.isPresent()) {
            super.setDriverClass("org.postgresql.Driver");
        } else {
            super.setDriverClass(driverClass);
        }
    }
}
