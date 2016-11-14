package com.ft.methodeimagesetmapper.health;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.ft.messagequeueproducer.health.QueueProxyHealthcheck;
import com.ft.platform.dropwizard.AdvancedHealthCheck;
import com.ft.platform.dropwizard.AdvancedResult;

import java.util.Optional;

public class CanConnectToMessageQueueProducerProxyHealthcheck
        extends AdvancedHealthCheck {

    private final QueueProxyHealthcheck queueProxyHealthcheck;
    private final HealthcheckConfiguration healthcheckConfiguration;
    private Timer timer;

    public CanConnectToMessageQueueProducerProxyHealthcheck(final QueueProxyHealthcheck queueProxyHealthcheck,
                                                            final HealthcheckConfiguration healthcheckConfiguration,
                                                            final MetricRegistry metrics) {

        super(healthcheckConfiguration.getName());
        this.queueProxyHealthcheck = queueProxyHealthcheck;
        this.healthcheckConfiguration = healthcheckConfiguration;
        if (metrics != null) {
            timer = metrics.timer(MetricRegistry.name(CanConnectToMessageQueueProducerProxyHealthcheck.class, "checkAdvanced"));
        } else {
            timer = new Timer();
        }
    }

    @Override
    protected AdvancedResult checkAdvanced() throws Exception {
        try (Timer.Context t = timer.time()) {
            Optional<QueueProxyHealthcheck.Unhealthy> health = queueProxyHealthcheck.check();
            if (health.isPresent()) {
                final QueueProxyHealthcheck.Unhealthy unhealthy = health.get();
                if (unhealthy.getThrowable() != null) {
                    return AdvancedResult.error(this, unhealthy.getThrowable());
                } else {
                    return AdvancedResult.error(this, unhealthy.getMessage());
                }
            }
            return AdvancedResult.healthy();
        }
    }

    @Override
    protected int severity() {
        return healthcheckConfiguration.getSeverity();
    }

    @Override
    protected String businessImpact() {
        return healthcheckConfiguration.getBusinessImpact();
    }

    @Override
    protected String technicalSummary() {
        return healthcheckConfiguration.getTechnicalSummary();
    }

    @Override
    protected String panicGuideUrl() {
        return healthcheckConfiguration.getPanicGuideUrl();
    }
}
