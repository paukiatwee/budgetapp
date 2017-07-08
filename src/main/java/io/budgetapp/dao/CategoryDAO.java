package io.budgetapp.dao;

import io.budgetapp.application.NotFoundException;
import io.budgetapp.configuration.AppConfiguration;
import io.budgetapp.model.Category;
import io.budgetapp.model.User;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 */
public class CategoryDAO extends DefaultDAO<Category> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryDAO.class);

    private final AppConfiguration configuration;

    public CategoryDAO(SessionFactory sessionFactory, AppConfiguration configuration) {
        super(sessionFactory);
        this.configuration = configuration;
    }

    public List<Category> findCategories(User user) {
        Criteria criteria = userCriteria(user);
        criteria.addOrder(Order.desc("type"));
        return list(criteria);
    }

    public Collection<Category> addDefaultCategories(User user) {
        Collection<Category> categories = new ArrayList<>(configuration.getCategories().size());
        configuration.getCategories()
                .forEach(c -> categories.add(addCategory(user, category(c))));
        return categories;
    }

    public Category addCategory(User user, Category category) {
        LOGGER.debug("Add new category {}", category);
        category.setUser(user);
        return persist(category);
    }

    public Category findById(long categoryId) {
        Category category = get(categoryId);
        if(category == null) {
            throw new NotFoundException();
        }

        return category;
    }

    private Category category(Category ori) {
        Category category = new Category();
        category.setName(ori.getName());
        category.setType(ori.getType());
        return category;
    }

    public Category find(User user, long categoryId) {
        Criteria criteria = userCriteria(user);
        criteria.add(Restrictions.eq("id", categoryId));

        return singleResult(criteria);
    }

    private Criteria defaultCriteria() {
        return criteria();
    }

    private Criteria userCriteria(User user) {
        Criteria criteria = defaultCriteria();
        criteria.add(Restrictions.eq("user", user));
        return criteria;
    }

    public void delete(Category category) {
        currentSession().delete(category);
    }

    public List<String> findSuggestions(User user, String q) {
        q = q == null? "": q.toLowerCase();

        Query<String> query = currentSession().createQuery("SELECT c.name FROM Category c WHERE c.user != :user AND LOWER(c.name) LIKE :q", String.class);
        query
                .setParameter("user", user)
                .setParameter("q", "%" + q + "%");

        return query.list();
    }
}
