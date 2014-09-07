package io.budgetapp.dao;

import io.budgetapp.util.Util;
import io.budgetapp.application.AccessDeniedException;
import io.budgetapp.application.NotFoundException;
import io.budgetapp.configuration.AppConfiguration;
import io.budgetapp.model.Ledger;
import io.budgetapp.model.User;
import io.budgetapp.application.AccessDeniedException;
import io.budgetapp.application.NotFoundException;
import io.budgetapp.configuration.AppConfiguration;
import io.budgetapp.model.Ledger;
import io.budgetapp.model.User;
import io.budgetapp.util.Util;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public class LedgerDAO extends AbstractDAO<Ledger> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LedgerDAO.class);

    private final AppConfiguration configuration;

    public LedgerDAO(SessionFactory sessionFactory, AppConfiguration configuration) {
        super(sessionFactory);
        this.configuration = configuration;
    }

    /**
     * add ledger for a given user
     * @param user owner
     * @param ledger new ledger
     * @return new ledger
     */
    public Ledger addLedger(User user, Ledger ledger) {
        LOGGER.debug("User {} add ledger {}", user, ledger);
        if(ledger.getPeriod() == null) {
            ledger.setPeriod(Util.currentYearMonth());
        }
        ledger.setUser(user);
        return persist(ledger);
    }

    /**
     * find ledgers for a given user for current month-year
     * @param user
     * @return
     */
    public List<Ledger> findLedgers(User user) {
        LocalDate now = LocalDate.now();
        return findLedgers(user, now.getMonthValue(), now.getYear());
    }

    /**
     * find ledgers for a given user for given month-year
     * @param user
     * @param month
     * @param year
     * @return
     */
    public List<Ledger> findLedgers(User user, int month, int year) {
        LOGGER.debug("Find ledgers by user {} by date {}-{}", user, month, year);
        Date yearMonth = Util.yearMonthDate(month, year);
        Criteria criteria = currentSession().createCriteria(Ledger.class);
        criteria.add(Restrictions.eq("user", user));
        criteria.add(Restrictions.eq("period", yearMonth));
        criteria.addOrder(Order.asc("id"));
        return list(criteria);
    }

    /**
     * find ledger by given id
     * @param ledgerId
     * @return
     */
    public Ledger findById(long ledgerId) {
        Ledger ledger = get(ledgerId);

        if(ledger == null) {
            throw new NotFoundException();
        }
        return ledger;
    }

    /**
     * find ledger for given user and id
     * @param user
     * @param ledgerId
     * @return
     */
    public Ledger findById(User user, Long ledgerId) {
        Ledger ledger = findById(ledgerId);
        if(!Objects.equals(user.getId(), ledger.getUser().getId())) {
            throw new AccessDeniedException();
        }
        return ledger;
    }

    public void update(Ledger ledger) {
        persist(ledger);
    }

    public void delete(Ledger ledger) {
        currentSession().delete(ledger);
    }
    public Map<String, List<Ledger>> findDefaultLedgers() {
        return configuration.getLedgers();
    }

    public List<Ledger> findByRange(User user, int startMonth, int startYear, int endMonth, int endYear) {
        Date start = Util.yearMonthDate(startMonth, startYear);
        Date end = Util.yearMonthDate(endMonth, endYear);
        Query query = currentSession().createQuery("FROM Ledger l WHERE l.user = :user AND l.period BETWEEN :start AND :end");
        query
                .setParameter("user", user)
                .setParameter("start", start)
                .setParameter("end", end);

        return list(query);
    }

    public Ledger findByLedgerType(Long ledgerTypeId) {
        Date now = Util.currentYearMonth();
        Query query = currentSession().createQuery("FROM Ledger l WHERE l.ledgerType.id = :ledgerTypeId AND l.period = :period");
        query
                .setParameter("ledgerTypeId", ledgerTypeId)
                .setParameter("period", now);
        return uniqueResult(query);
    }

    public List<Ledger> findByUserAndCategory(User user, long categoryId) {
        Criteria criteria = userCriteria(user);
        criteria.add(Restrictions.eq("category.id", categoryId));
        criteria.add(Restrictions.eq("period", Util.currentYearMonth()));
        return list(criteria);
    }

    public List<String> findSuggestions(User user, String q) {
        q = q == null? "": q.toLowerCase();

        Query query = currentSession().createQuery("SELECT l.name FROM Ledger l WHERE l.user != :user AND LOWER(l.name) LIKE :q");
        query
                .setParameter("user", user)
                .setParameter("q", "%" + q + "%");

        return (List<String>)query.list();
    }

    private Criteria defaultCriteria() {
        return currentSession().createCriteria(Ledger.class);
    }

    private Criteria userCriteria(User user) {
        Criteria criteria = defaultCriteria();
        criteria.add(Restrictions.eq("user", user));
        return criteria;
    }

}
