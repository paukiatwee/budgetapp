package io.budgetapp.model;

/**
 *
 */
public enum CategoryType {

    INCOME("Income"),
    EXPENSE("Expense");

    private final String display;

    CategoryType(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}
