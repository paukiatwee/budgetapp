package io.budgetapp.job;

import io.budgetapp.service.FinanceService;

/**
 *
 */
public class RecurringJob extends Job {

    private final FinanceService financeService;

    public RecurringJob(FinanceService financeService) {
        super(financeService.getSessionFactory());
        this.financeService = financeService;
    }

    @Override
    public void doRun() {
        financeService.updateRecurrings();
    }

    @Override
    public String getName() {
        return "Recurring job";
    }
}
