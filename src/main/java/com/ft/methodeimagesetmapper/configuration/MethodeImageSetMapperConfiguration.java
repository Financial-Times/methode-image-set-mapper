package com.ft.methodeimagesetmapper.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class MethodeImageSetMapperConfiguration extends Configuration {

    private final ConsumerConfiguration consumer;
    private final ProducerConfiguration producer;
    private final String contentUriPrefix;

    public MethodeImageSetMapperConfiguration(@JsonProperty("consumer") ConsumerConfiguration consumer,
                                              @JsonProperty("producer") ProducerConfiguration producer,
                                              @JsonProperty("contentUriPrefix") String contentUriPrefix) {
        this.consumer = consumer;
        this.producer = producer;
        this.contentUriPrefix = contentUriPrefix;
    }

    public ConsumerConfiguration getConsumerConfiguration() {
        return consumer;
    }

    public ProducerConfiguration getProducerConfiguration() {
        return producer;
    }

    public String getContentUriPrefix() {
        return contentUriPrefix;
    }
}
