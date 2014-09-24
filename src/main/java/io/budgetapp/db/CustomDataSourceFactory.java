package io.budgetapp.db;

import io.budgetapp.util.Util;
import io.dropwizard.db.DataSourceFactory;

import java.net.URI;

/**
 * Added support to get DB info from env, if any
 */
public class CustomDataSourceFactory extends DataSourceFactory {

    private boolean isHeroku() {
        return System.getenv("DATABASE_URL") != null;
    }

    @Override
    public String getUser() {
        if(isHeroku()) {
            return Util.getDatabaseURL().getUserInfo().split(":")[0];
        } else {
            return super.getUser();
        }
    }

    @Override
    public String getPassword() {
        if(isHeroku()) {
            return Util.getDatabaseURL().getUserInfo().split(":")[1];
        } else {
            return super.getPassword();
        }
    }

    @Override
    public String getUrl() {
        if(isHeroku()) {
            URI dbUri = Util.getDatabaseURL();
            return "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
        } else {
            return super.getUrl();
        }
    }
}
