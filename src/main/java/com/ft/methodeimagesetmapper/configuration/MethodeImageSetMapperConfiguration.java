package com.ft.methodeimagesetmapper.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.platform.dropwizard.AppInfo;
import com.ft.platform.dropwizard.ConfigWithAppInfo;
import io.dropwizard.Configuration;

public class MethodeImageSetMapperConfiguration extends Configuration implements ConfigWithAppInfo {

    private final ConsumerConfiguration consumer;
    private final ProducerConfiguration producer;
    private final String contentUriPrefix;

    @JsonProperty
    private AppInfo appInfo = new AppInfo();

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

    @Override
    public AppInfo getAppInfo() {
        return appInfo;
    }
}
