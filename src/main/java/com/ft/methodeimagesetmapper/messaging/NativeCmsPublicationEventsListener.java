package com.ft.methodeimagesetmapper.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.message.consumer.MessageListener;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.methodeimagesetmapper.exception.IngesterException;
import com.ft.methodeimagesetmapper.model.EomFile;
import com.ft.methodeimagesetmapper.service.ContentMapper;
import com.ft.methodeimagesetmapper.validation.PublishingValidator;
import com.ft.methodeimagesetmapper.validation.UuidValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Predicate;

import static com.ft.methodeimagesetmapper.util.ImageSetUuidGenerator.fromImageUuid;
import static com.ft.methodeimagesetmapper.util.ImageSetUuidGenerator.originalImageUuid;

public class NativeCmsPublicationEventsListener implements MessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(NativeCmsPublicationEventsListener.class);

    private final Predicate<Message> filter;
    private final ContentMapper mapper;
    private final ObjectMapper objectMapper;
    private final SystemId systemId;
    private final UuidValidator uuidValidator;
    private final PublishingValidator publishingValidator;

    public NativeCmsPublicationEventsListener(String systemCode, ContentMapper mapper, ObjectMapper objectMapper,
                                              UuidValidator uuidValidator, PublishingValidator publishingValidator) {
        this.systemId = SystemId.systemIdFromCode(systemCode);
        this.filter = msg -> (systemId.equals(msg.getOriginSystemId()));
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.uuidValidator = uuidValidator;
        this.publishingValidator = publishingValidator;
    }

    @Override
    public boolean onMessage(Message message, String transactionId) {
        if (filter.test(message)) {
            LOG.info("process message");

            try {
                EomFile methodeContent = objectMapper.reader(EomFile.class).readValue(message.getMessageBody());
                uuidValidator.validate(methodeContent.getUuid());
                if (publishingValidator.isValidForPublishing(methodeContent)) {
                    String uuid = fromImageUuid(UUID.fromString(methodeContent.getUuid())).toString();
                    LOG.info("Importing content [{}] of type [{}] as image set [{}].",
                            methodeContent.getUuid(), methodeContent.getType(), uuid);
                    LOG.info("Event for {}.", methodeContent.getUuid());
                    mapper.mapImageSet(uuid, methodeContent, transactionId, message.getMessageTimestamp());
                } else {
                    LOG.info("Skip message [{}] of type [{}]", methodeContent.getUuid(), methodeContent.getType());
                }
            } catch (IOException e) {
                throw new IngesterException("Unable to parse Methode content message", e);
            }
        } else {
            LOG.info("skip message");
            LOG.debug("skip message {}", message);
        }
        return true;
    }

}
