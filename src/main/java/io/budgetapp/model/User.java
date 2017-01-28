package io.budgetapp.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import liquibase.util.MD5Util;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.io.Serializable;
import java.security.Principal;
import java.util.Date;

/**
 *
 */
@Entity
@Table(name = "users")
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Principal, Serializable {

    private static final long serialVersionUID = 3868269731826822792L;
    private Long id;
    private String username;
    private String password;
    private String name;
    private Date createdAt;
    private String currency;

    // not in DB
    private String token;
    // end

    public User() {
    }

    public User(long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", insertable = false, nullable = false, updatable = false)
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    // not in DB
    @Transient
    @JsonProperty("avatar")
    public String getAvatar() {
        return "https://www.gravatar.com/avatar/" + MD5Util.computeMD5(getUsername());
    }

    @Transient
    @JsonProperty("token")
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    // end

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("id=").append(id);
        sb.append(", username='").append(username).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
