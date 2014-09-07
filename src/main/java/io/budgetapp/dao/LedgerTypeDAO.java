package io.budgetapp.dao;

import io.budgetapp.model.LedgerType;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

/**
 *
 */
public class LedgerTypeDAO extends AbstractDAO<LedgerType> {

    public LedgerTypeDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public LedgerType addLedgerType() {
        LedgerType ledgerType = new LedgerType();
        return persist(ledgerType);
    }
}
