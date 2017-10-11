package io.budgetapp.dao;

import io.budgetapp.model.Budget;
import io.budgetapp.util.Util;
import io.budgetapp.application.AccessDeniedException;
import io.budgetapp.application.NotFoundException;
import io.budgetapp.configuration.AppConfiguration;
import io.budgetapp.model.User;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public class BudgetDAO extends AbstractDAO<Budget> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BudgetDAO.class);

    private final AppConfiguration configuration;

    public BudgetDAO(SessionFactory sessionFactory, AppConfiguration configuration) {
        super(sessionFactory);
        this.configuration = configuration;
    }

    /**
     * add budget for a given user
     * @param user owner
     * @param budget new budget
     * @return new budget
     */
    public Budget addBudget(User user, Budget budget) {
        LOGGER.debug("User {} add budget {}", user, budget);
        if(budget.getPeriod() == null) {
            budget.setPeriod(Util.currentYearMonth());
        }
        budget.setUser(user);
        return persist(budget);
    }

    /**
     * find budgets for a given user for current month-year
     * @param user
     * @return
     */
    public List<Budget> findBudgets(User user) {
        LocalDate now = LocalDate.now();
        return findBudgets(user, now.getMonthValue(), now.getYear(), false);
    }

    /**
     * find budgets for a given user for given month-year
     * @param user
     * @param month
     * @param year
     * @param lazy
     * @return
     */
    public List<Budget> findBudgets(User user, int month, int year, boolean lazy) {
        LOGGER.debug("Find budgets by user {} by date {}-{}", user, month, year);
        Date yearMonth = Util.yearMonthDate(month, year);
        Criteria criteria = criteria();
        if(!lazy) {
            criteria.setFetchMode("category", FetchMode.JOIN);
        }
        criteria.add(Restrictions.eq("user", user));
        criteria.add(Restrictions.eq("period", yearMonth));
        criteria.addOrder(Order.asc("id"));
        return list(criteria);
    }

    public Date findLatestBudget(User user) {
        LOGGER.debug("Find latest budget by user {}", user);
        Criteria criteria = criteria();
        criteria.add(Restrictions.eq("user", user));
        criteria.setProjection(Projections.max("createdAt"));
        criteria.setMaxResults(1);
        return (Date)criteria.uniqueResult();
    }

    /**
     * Throws an error if the budget does not belong to the user
     * @param user
     * @param budget
     * @throws AccessDeniedException
     */
    private void checkBudget(User user, Budget budget) throws AccessDeniedException {
        if(!Objects.equals(user.getId(), budget.getUser().getId())) {
            throw new AccessDeniedException();
        }
    }

    /**
     * find budget by given id
     * @param budgetId
     * @return
     */
    public Budget findById(long budgetId) {
        Budget budget = get(budgetId);

        if(budget == null) {
            throw new NotFoundException();
        }
        return budget;
    }

    /**
     * find budget for given user and id
     * @param user
     * @param budgetId
     * @return
     */
    public Budget findById(User user, Long budgetId) {
        Budget budget = findById(budgetId);
        checkBudget(user, budget);
        return budget;
    }

    /**
     * find all budgets for given ids
     * @param budgetIds
     * @return
     */
    public List<Budget> findByIds(Collection<Long> budgetIds) {
        Criteria criteria = criteria();
        criteria.add(Restrictions.in("id", budgetIds));
        return list(criteria);
    }

    /**
     * find all budgets for given user and ids
     * @param user
     * @param budgetIds
     * @return
     */
    public List<Budget> findByIds(User user, Collection<Long> budgetIds) {
        List<Budget> budgets = findByIds(budgetIds);
        for (Budget budget : budgets) {
            checkBudget(user, budget);
        }
        return budgets;
    }

    public void update(Budget budget) {
        persist(budget);
    }

    public void delete(Budget budget) {
        currentSession().delete(budget);
    }
    public Map<String, List<Budget>> findDefaultBudgets() {
        return configuration.getBudgets();
    }

    public List<Budget> findByRange(User user, int startMonth, int startYear, int endMonth, int endYear) {
        Date start = Util.yearMonthDate(startMonth, startYear);
        Date end = Util.yearMonthDate(endMonth, endYear);
        Query<Budget> query = query("FROM Budget b WHERE b.user = :user AND b.period BETWEEN :start AND :end");
        query
                .setParameter("user", user)
                .setParameter("start", start)
                .setParameter("end", end);

        return list(query);
    }

    public Budget findByBudgetType(Long budgetTypeId) {
        Date now = Util.currentYearMonth();
        Query<Budget> query = query("FROM Budget b WHERE b.budgetType.id = :budgetTypeId AND b.period = :period");
        query
                .setParameter("budgetTypeId", budgetTypeId)
                .setParameter("period", now);
        return uniqueResult(query);
    }

    public List<Budget> findByUserAndCategory(User user, long categoryId) {
        Criteria criteria = userCriteria(user);
        criteria.add(Restrictions.eq("category.id", categoryId));
        criteria.add(Restrictions.eq("period", Util.currentYearMonth()));
        return list(criteria);
    }

    public List<String> findSuggestions(User user, String q) {
        q = q == null? "": q.toLowerCase();

        Query<String> query = currentSession().createQuery("SELECT b.name FROM Budget l WHERE b.user != :user AND LOWER(b.name) LIKE :q", String.class);
        query
                .setParameter("user", user)
                .setParameter("q", "%" + q + "%");

        return query.list();
    }

    private Criteria defaultCriteria() {
        return criteria();
    }

    private Criteria userCriteria(User user) {
        Criteria criteria = defaultCriteria();
        criteria.add(Restrictions.eq("user", user));
        return criteria;
    }

}
