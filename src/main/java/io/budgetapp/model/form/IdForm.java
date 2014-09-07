package io.budgetapp.model.form;

import java.io.Serializable;

/**
 *
 */
public class IdForm implements Serializable {

    private static final long serialVersionUID = 7501898815842450213L;

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
