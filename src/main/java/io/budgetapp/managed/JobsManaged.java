package io.budgetapp.managed;

import io.budgetapp.job.RecurringJob;
import io.budgetapp.service.FinanceService;
import io.dropwizard.lifecycle.Managed;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class JobsManaged implements Managed {

    private final Timer timer = new Timer();
    private final FinanceService financeService;

    public JobsManaged(FinanceService financeService) {
        this.financeService = financeService;
    }

    @Override
    public void start() throws Exception {
        timer.scheduleAtFixedRate(new RecurringJob(financeService), 10 * 1000, TimeUnit.SECONDS.toMillis(10));
    }

    @Override
    public void stop() throws Exception {
        timer.cancel();
    }
}
