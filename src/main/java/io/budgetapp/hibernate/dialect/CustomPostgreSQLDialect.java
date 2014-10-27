package io.budgetapp.hibernate.dialect;

import org.hibernate.dialect.PostgreSQL9Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

/**
 * Support WEEK(DATETIME)
 */
public class CustomPostgreSQLDialect extends PostgreSQL9Dialect {

    public CustomPostgreSQLDialect() {
        super();
        registerFunction("dayofweek", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "extract(dow from ?1)") );
        registerFunction( "week", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "extract(week from ?1)") );
    }
}
