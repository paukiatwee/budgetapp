package io.budgetapp.dao;

import com.google.common.base.Optional;
import io.budgetapp.model.AuthToken;
import io.budgetapp.model.User;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import java.util.List;
import java.util.UUID;

/**
 *
 */
public class AuthTokenDAO extends AbstractDAO<AuthToken> {

    public AuthTokenDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
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
        return Optional.fromNullable(uniqueResult(criteria));
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
