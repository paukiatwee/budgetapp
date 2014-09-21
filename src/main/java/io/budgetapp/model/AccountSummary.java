package io.budgetapp.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class AccountSummary implements Serializable {

    private static final long serialVersionUID = 5319703962527161534L;

    private List<Budget> income;
    private List<Group> groups = new ArrayList<>();

    public List<Budget> getIncome() {
        return income;
    }

    public void setIncome(List<Budget> income) {
        this.income = income;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }
}
