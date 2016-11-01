package com.ft.methodeimagesetmapper.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.message.consumer.config.HealthcheckConfiguration;
import com.ft.message.consumer.config.MessageQueueConsumerConfiguration;
import io.dropwizard.client.JerseyClientConfiguration;

public class ConsumerConfiguration {

    private final JerseyClientConfiguration jerseyConfig;
    private final MessageQueueConsumerConfiguration consumerConfig;
    private final HealthcheckConfiguration healthcheckConfig;
    private final String systemCode;

    public ConsumerConfiguration(@JsonProperty("jerseyClient") JerseyClientConfiguration jerseyConfig,
                                 @JsonProperty("messageConsumer") MessageQueueConsumerConfiguration consumerConfig,
                                 @JsonProperty("healthCheck") HealthcheckConfiguration healthCheckConfig,
                                 @JsonProperty("systemCode") String systemCode) {

        this.jerseyConfig = jerseyConfig;
        this.consumerConfig = consumerConfig;
        this.healthcheckConfig = healthCheckConfig;
        this.systemCode = systemCode;
    }

    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return jerseyConfig;
    }

    public MessageQueueConsumerConfiguration getMessageQueueConsumerConfiguration() {
        return consumerConfig;
    }

    public HealthcheckConfiguration getHealthcheckConfiguration() {
        return healthcheckConfig;
    }

    public String getSystemCode() {
        return systemCode;
    }
}
