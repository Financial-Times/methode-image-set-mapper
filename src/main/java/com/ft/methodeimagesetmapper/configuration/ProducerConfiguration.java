package com.ft.methodeimagesetmapper.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.messagequeueproducer.QueueProxyConfiguration;
import com.ft.methodeimagesetmapper.health.HealthcheckConfiguration;
import io.dropwizard.client.JerseyClientConfiguration;

public class ProducerConfiguration {

    private final JerseyClientConfiguration jerseyConfig;
    private final QueueProxyConfiguration producerConfig;
    private final HealthcheckConfiguration healthcheckConfig;

    public ProducerConfiguration(@JsonProperty("jerseyClient") JerseyClientConfiguration jerseyConfig,
                                 @JsonProperty("messageProducer") QueueProxyConfiguration producerConfig,
                                 @JsonProperty("healthCheck") HealthcheckConfiguration healthcheckConfig) {

        this.jerseyConfig = jerseyConfig;
        this.producerConfig = producerConfig;
        this.healthcheckConfig = healthcheckConfig;
    }

    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return jerseyConfig;
    }

    public QueueProxyConfiguration getMessageQueueProducerConfiguration() {
        return producerConfig;
    }

    public HealthcheckConfiguration getHealthcheckConfiguration() {
        return healthcheckConfig;
    }
}
