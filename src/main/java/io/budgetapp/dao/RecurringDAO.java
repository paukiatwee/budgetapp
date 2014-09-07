package io.budgetapp.dao;

import io.budgetapp.model.Recurring;
import io.budgetapp.model.RecurringType;
import io.budgetapp.model.User;
import io.budgetapp.model.Recurring;
import io.budgetapp.model.RecurringType;
import io.budgetapp.model.User;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.List;

/**
 *
 */
public class RecurringDAO extends AbstractDAO<Recurring> {

    public RecurringDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Recurring addRecurring(Recurring recurring) {
        return persist(recurring);
    }

    public List<Recurring> findRecurrings(User user) {
        return currentSession().createQuery("SELECT r FROM Recurring r JOIN r.ledgerType ledgerType WHERE ledgerType IN (FROM Ledger ledger WHERE ledger.user = :user)")
                .setParameter("user", user)
                .list();
    }

    public void delete(Recurring recurring) {
        currentSession().delete(recurring);
    }

    public Recurring find(User user, long recurringId) {
        return (Recurring)currentSession().createQuery("SELECT r FROM Recurring r JOIN r.ledgerType ledgerType WHERE ledgerType IN (FROM Ledger ledger WHERE ledger.user = :user and r.id = :id)")
                .setParameter("user", user)
                .setParameter("id", recurringId).uniqueResult();
    }

    public List<Recurring> findByLedgerTypeId(long ledgerTypeId) {
        return currentSession().createQuery("SELECT r FROM Recurring r WHERE r.ledgerType.id = :ledgerTypeId")
                .setParameter("ledgerTypeId", ledgerTypeId)
                .list();
    }

    public List<Recurring> findActiveRecurrings() {
        LocalDate now = LocalDate.now();

        return currentSession()
                .createQuery("SELECT r FROM Recurring r WHERE " +
                        "(r.recurringType = :daily AND DAY(r.lastRunAt) = :yesterday) OR " +
                        "(r.recurringType = :weekly AND WEEK(r.lastRunAt) = :lastWeek) OR " +
                        "(r.recurringType = :monthly AND MONTH(r.lastRunAt) = :lastMonth) OR " +
                        "(r.recurringType = :yearly AND YEAR(r.lastRunAt) = :lastYear) OR " +
                        "r.lastRunAt IS NULL")
                .setParameter("daily", RecurringType.DAILY)
                .setParameter("yesterday", now.minusDays(1).getDayOfMonth())
                .setParameter("weekly", RecurringType.WEEKLY)
                .setParameter("lastWeek", now.minusWeeks(1).get(ChronoField.ALIGNED_WEEK_OF_YEAR))
                .setParameter("monthly", RecurringType.MONTHLY)
                .setParameter("lastMonth", now.minusMonths(1).getMonthValue())
                .setParameter("yearly", RecurringType.YEARLY)
                .setParameter("lastYear", now.minusYears(1).getYear())
                .list();
    }

    public void update(Recurring recurring) {
        persist(recurring);
    }
}
