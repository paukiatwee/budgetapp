package io.budgetapp.dao;

import io.budgetapp.application.NotFoundException;
import io.budgetapp.model.User;
import io.budgetapp.model.form.SignUpForm;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 *
 */
public class UserDAO extends AbstractDAO<User> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDAO.class);

    public UserDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public User findById(Long userId) {
        User user = get(userId);
        if(user == null) {
            throw new NotFoundException();
        }
        return user;
    }

    public User add(SignUpForm signUp) {
        LOGGER.debug("Add new user {}", signUp);
        User user = new User();
        user.setUsername(signUp.getUsername());
        user.setPassword(signUp.getPassword());
        user = persist(user);
        return user;
    }

    public void update(User user) {
        LOGGER.debug("Update user {}", user);
        persist(user);
    }

    public Optional<User> findByUsername(String username) {
        Criteria criteria = criteria();
        criteria.add(Restrictions.eq("username", username).ignoreCase());
        List<User> users = list(criteria);
        if(users.size() == 1) {
            return Optional.of(users.get(0));
        } else {
            return Optional.empty();
        }
    }
}
