package io.budgetapp.managed;

import io.budgetapp.job.RecurringJob;
import io.dropwizard.lifecycle.Managed;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class JobsManaged implements Managed {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final RecurringJob recurringJob;

    public JobsManaged(RecurringJob recurringJob) {
        this.recurringJob = recurringJob;
    }

    @Override
    public void start() throws Exception {
        scheduler.scheduleAtFixedRate(recurringJob, 0, 10, TimeUnit.SECONDS);
    }

    @Override
    public void stop() throws Exception {
        scheduler.shutdown();
    }
}
