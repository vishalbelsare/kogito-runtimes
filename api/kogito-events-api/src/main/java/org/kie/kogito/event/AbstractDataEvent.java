/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.event;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.kie.kogito.event.cloudevents.CloudEventExtensionConstants;
import org.kie.kogito.event.cloudevents.SpecVersionDeserializer;
import org.kie.kogito.event.cloudevents.SpecVersionSerializer;
import org.kie.kogito.event.cloudevents.utils.CloudEventUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.cloudevents.SpecVersion;

/**
 * This is an abstract implementation of the {@link DataEvent} that contains basic common attributes referring to
 * kogito processes metadata. This class can be extended mainly by Services that need to publish events to be
 * indexed by the Data-Index service.
 *
 * @param <T> the payload
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractDataEvent<T> implements DataEvent<T> {

    /**
     * String prefix for Kogito CloudEvents type fields.
     * Since this is a required field, the constructor will fill them with this default value.
     * Ideally, callers would use #TYPE_FORMAT to fill this field using the process name and the signal node name, e.g: process.travelagency.visaapproved
     */
    public static final String TYPE_PREFIX = "process";
    public static final String TYPE_FORMAT = TYPE_PREFIX + ".%s.%s";
    /**
     * String format for Kogito CloudEvents source fields.
     * Since this is a required field, the constructor will fill them with default value, e.g.: /process/travelagency
     * See more about the source format: https://github.com/cloudevents/spec/blob/v1.0/spec.md#source-1
     */
    public static final String SOURCE_FORMAT = "/process/%s";
    public static final String SPEC_VERSION = "1.0";

    @JsonDeserialize(using = SpecVersionDeserializer.class)
    @JsonSerialize(using = SpecVersionSerializer.class)
    @JsonProperty("specversion")
    private SpecVersion specVersion;

    private String id;

    private URI source;

    private String type;

    private OffsetDateTime time;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String subject;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String dataContentType;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private URI dataSchema;

    private T data;

    @JsonProperty(CloudEventExtensionConstants.PROCESS_INSTANCE_ID)
    private String kogitoProcessinstanceId;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty(CloudEventExtensionConstants.PROCESS_ROOT_PROCESS_INSTANCE_ID)
    private String kogitoRootProcessinstanceId;

    @JsonProperty(CloudEventExtensionConstants.PROCESS_ID)
    private String kogitoProcessId;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty(CloudEventExtensionConstants.PROCESS_ROOT_PROCESS_ID)
    private String kogitoRootProcessId;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty(CloudEventExtensionConstants.ADDONS)
    private String kogitoAddons;

    public AbstractDataEvent() {
    }

    public AbstractDataEvent(String type,
            String source,
            T body,
            String kogitoProcessinstanceId,
            String kogitoRootProcessinstanceId,
            String kogitoProcessId,
            String kogitoRootProcessId,
            String kogitoAddons) {
        this.specVersion = SpecVersion.parse(SPEC_VERSION);
        this.id = UUID.randomUUID().toString();
        this.source = Optional.ofNullable(source).map(URI::create).orElse(null);
        this.type = type;
        this.time = ZonedDateTime.now().toOffsetDateTime();
        this.data = body;

        this.kogitoProcessinstanceId = kogitoProcessinstanceId;
        this.kogitoRootProcessinstanceId = kogitoRootProcessinstanceId;
        this.kogitoProcessId = kogitoProcessId;
        this.kogitoRootProcessId = kogitoRootProcessId;
        this.kogitoAddons = kogitoAddons;

        this.ensureRequiredFields();
    }

    public AbstractDataEvent(String type,
            String source,
            T body,
            String kogitoProcessinstanceId,
            String kogitoRootProcessinstanceId,
            String kogitoProcessId,
            String kogitoRootProcessId,
            String kogitoAddons,
            String subject,
            String dataContentType,
            String dataSchema) {
        this(type, source, body, kogitoProcessinstanceId, kogitoRootProcessinstanceId, kogitoProcessId, kogitoRootProcessId, kogitoAddons);
        this.subject = subject;
        this.dataContentType = dataContentType;
        this.dataSchema = URI.create(dataSchema);
    }

    protected void ensureRequiredFields() {
        if (this.type == null || this.type.isEmpty()) {
            this.type = TYPE_PREFIX;
        }
        if (this.source == null || this.source.toString().isEmpty()) {
            this.source = URI.create(String.format(SOURCE_FORMAT, kogitoProcessId));
        }
    }

    @Override
    public URI getSource() {
        return source;
    }

    @Override
    public SpecVersion getSpecVersion() {
        return specVersion;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public OffsetDateTime getTime() {
        return time;
    }

    @Override
    public T getData() {
        return data;
    }

    @Override
    public String getDataContentType() {
        return dataContentType;
    }

    @Override
    public URI getDataSchema() {
        return dataSchema;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    public String getKogitoProcessinstanceId() {
        return kogitoProcessinstanceId;
    }

    public String getKogitoRootProcessinstanceId() {
        return kogitoRootProcessinstanceId;
    }

    public String getKogitoProcessId() {
        return kogitoProcessId;
    }

    public String getKogitoRootProcessId() {
        return kogitoRootProcessId;
    }

    public String getKogitoAddons() {
        return kogitoAddons;
    }

    @Override
    public Object getAttribute(String name) throws IllegalArgumentException {
        return CloudEventUtils.getAttribute(name, this);
    }

    //TODO: missing additional attributes as extensions for CloudEvent API.
    @Override
    public Object getExtension(String s) {
        return null;
    }

    @JsonIgnore
    @Override
    public Set<String> getAttributeNames() {
        return DataEvent.super.getAttributeNames();
    }

    @JsonIgnore
    @Override
    public Set<String> getExtensionNames() {
        return Collections.emptySet();
    }
}
