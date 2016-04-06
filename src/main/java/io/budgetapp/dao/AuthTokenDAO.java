package io.budgetapp.dao;

import io.budgetapp.model.AuthToken;
import io.budgetapp.model.User;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 *
 */
public class AuthTokenDAO extends AbstractDAO<AuthToken> {

    private final SessionFactory sessionFactory;

    public AuthTokenDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
        this.sessionFactory = sessionFactory;
    }

    public AuthToken add(User user) {
        AuthToken authToken = new AuthToken();
        authToken.setUser(user);
        authToken.setToken(newToken());
        return persist(authToken);
    }

    public Optional<AuthToken> find(String token) {
        Criteria criteria = currentSession().createCriteria(AuthToken.class);
        criteria.add(Restrictions.eq("token", token));
        Optional<AuthToken> result = Optional.ofNullable(uniqueResult(criteria));
        return result;
    }

    public List<AuthToken> findByUser(User user) {
        Criteria criteria = currentSession().createCriteria(AuthToken.class);
        criteria.add(Restrictions.eq("user", user));
        return list(criteria);
    }

    private String newToken() {
        return UUID.randomUUID().toString();
    }
}
