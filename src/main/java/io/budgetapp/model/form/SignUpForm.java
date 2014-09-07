package io.budgetapp.model.form;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 *
 */
public class SignUpForm implements Serializable {

    private static final long serialVersionUID = -3387043933342205884L;

    private String username;
    private String password;

    @NotBlank(message = "{validation.username.required}")
    @Email(message = "{validation.username.invalid}")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @NotBlank(message = "{validation.password.required}")
    @Size.List({@Size(min = 6, message = "{validation.password.length}")})
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SignUpForm{");
        sb.append("username='").append(username).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
