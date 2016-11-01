package com.ft.methodeimagesetmapper.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EomFile {

    private final String uuid;
    private final String type;
    private final byte[] value;
    private final String attributes;
    private final String workflowStatus;
    private final String systemAttributes;
    private final String usageTickets;
    private Date lastModified;

    public EomFile(@JsonProperty("uuid") String uuid,
                   @JsonProperty("type") String type,
                   @JsonProperty("value") byte[] bytes,
                   @JsonProperty("attributes") String attributes,
                   @JsonProperty("workflowStatus") String workflowStatus,
                   @JsonProperty("systemAttributes") String systemAttributes,
                   @JsonProperty("usageTickets") String usageTickets,
                   @JsonProperty("lastModified") Date lastModified) {
        this.uuid = uuid;
        this.type = type;
        this.value = bytes;
        this.attributes = attributes;
        this.workflowStatus = workflowStatus;
        this.systemAttributes = systemAttributes;
        this.usageTickets = usageTickets;
        this.lastModified = lastModified;
    }

    public String getUuid() {
        return uuid;
    }

    public String getType() {
        return type;
    }

    public byte[] getValue() {
        return value;
    }

    public String getAttributes() {
        return attributes;
    }

    public String getWorkflowStatus() {
        return workflowStatus;
    }

    public String getSystemAttributes() {
        return systemAttributes;
    }

    public String getUsageTickets() {
        return usageTickets;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public static class Builder {
        private String uuid;
        private String type;
        private byte[] value;
        private String attributes;
        private String workflowStatus;
        private String systemAttributes;
        private String usageTickets;
        private Date lastModified;

        public Builder withUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withValue(byte[] value) {
            this.value = value;
            return this;
        }

        public Builder withAttributes(String attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder withWorkflowStatus(String workflowStatus) {
            this.workflowStatus = workflowStatus;
            return this;
        }

        public Builder withSystemAttributes(String systemAttributes) {
            this.systemAttributes = systemAttributes;
            return this;
        }

        public Builder withUsageTickets(String usageTickets) {
            this.usageTickets = usageTickets;
            return this;
        }

        public Builder builder(Date lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public com.ft.methodeimagesetmapper.model.EomFile build() {
            return new com.ft.methodeimagesetmapper.model.EomFile(uuid, type, value, attributes, workflowStatus,
                    systemAttributes, usageTickets, lastModified);
        }
    }
}
