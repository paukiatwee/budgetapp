package io.budgetapp.job;

import org.glassfish.jersey.server.internal.process.MappableException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public abstract class Job implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Job.class);

    private final SessionFactory sessionFactory;

    public Job(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public abstract void doRun();
    public abstract String getName();

    @Override
    public final void run() {
        long start = System.currentTimeMillis();
        LOGGER.debug("Start {} job", getName());
        final Session session = sessionFactory.openSession();
        try {
            ManagedSessionContext.bind(session);
            session.beginTransaction();
            try {
                doRun();
                final Transaction txn = session.getTransaction();
                if (txn != null && txn.getStatus() != TransactionStatus.ACTIVE) {
                    txn.commit();
                }
            } catch (Exception e) {
                final Transaction txn = session.getTransaction();
                if (txn != null && txn.getStatus() != TransactionStatus.ACTIVE) {
                    txn.rollback();
                }
                throw new MappableException(e);
            }
        } finally {
            session.close();
            ManagedSessionContext.unbind(sessionFactory);
        }
        LOGGER.debug("Complete {} job and took {}ms", getName(), System.currentTimeMillis() - start);
    }
}
