package io.budgetapp.hibernate.dialect;

import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

/**
 * Support WEEK(DATETIME)
 */
public class CustomHSQLDialect extends HSQLDialect {

    public CustomHSQLDialect() {
        super();
        registerFunction( "week", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "extract(week_of_year from ?1)") );
    }
}
