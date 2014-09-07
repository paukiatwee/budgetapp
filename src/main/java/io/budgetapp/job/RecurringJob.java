package io.budgetapp.job;

import io.budgetapp.service.FinanceService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

/**
 *
 */
public class RecurringJob extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecurringJob.class);

    private final FinanceService financeService;

    public RecurringJob(FinanceService financeService) {
        this.financeService = financeService;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        LOGGER.debug("Start recurring job");
        SessionFactory sessionFactory = financeService.getSessionFactory();
        final Session session = sessionFactory.openSession();
        try {
            ManagedSessionContext.bind(session);
            session.beginTransaction();
            try {
                financeService.updateRecurrings();
                final Transaction txn = session.getTransaction();
                if (txn != null && txn.isActive()) {
                    txn.commit();
                }
            } catch (Exception e) {
                final Transaction txn = session.getTransaction();
                if (txn != null && txn.isActive()) {
                    txn.rollback();
                }
                this.<RuntimeException>rethrow(e);
            }
        } finally {
            session.close();
            ManagedSessionContext.unbind(sessionFactory);
        }
        LOGGER.debug("Complete recurring job and took {}ms", System.currentTimeMillis() - start);
    }

    @SuppressWarnings("unchecked")
    private <E extends Exception> void rethrow(Exception e) throws E {
        throw (E) e;
    }
}
