package com.ft.methodeimagesetmapper.messaging;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ft.content.model.Content;
import com.ft.messagequeueproducer.MessageProducer;
import com.ft.messagequeueproducer.model.KeyedMessage;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.messaging.standards.message.v1.MessageType;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.methodeimagesetmapper.exception.ContentMapperException;
import com.ft.methodeimagesetmapper.model.EomFile;
import com.ft.methodeimagesetmapper.service.ContentMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.UriBuilder;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.ft.api.util.transactionid.TransactionIdUtils.TRANSACTION_ID_HEADER;
import static com.ft.messaging.standards.message.v1.MediaType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageProducingContentMapperTest {

    private static final ObjectMapper JACKSON_MAPPER = new ObjectMapper();
    private static final SystemId SYSTEM_ID = SystemId.systemId("junit_system");
    private static final UriBuilder URI_BUILDER = UriBuilder.fromUri("http://www.example.org/content").path("{uuid}");
    private static final String TITLE = "Test Content";
    private static final String PUBLISH_REF = "junit12345";
    private static final MessageType CMS_CONTENT_PUBLISHED = MessageType.messageType("cms-content-published");

    private MessageProducingContentMapper mapper;

    @Mock
    private ContentMapper delegate;
    @Mock
    private MessageProducer producer;
    @Mock
    private EomFile incoming;

    @SuppressWarnings("unchecked")
    @Test
    public void thatMessageIsProducedAndSent() throws Exception {
        mapper = new MessageProducingContentMapper(delegate, JACKSON_MAPPER, SYSTEM_ID.toString(), producer, URI_BUILDER);

        UUID uuid = UUID.randomUUID();
        Date lastModified = new Date();
        Content content = new Content.Builder()
                .withUuid(uuid)
                .withTitle(TITLE)
                .withPublishReference(PUBLISH_REF)
                .withLastModified(lastModified)
                .build();

        when(delegate.mapImageSet(eq(uuid.toString()), any(EomFile.class), eq(PUBLISH_REF), eq(lastModified))).thenReturn(content);

        Content actual = mapper.mapImageSet(uuid.toString(), incoming, PUBLISH_REF, lastModified);

        assertThat(actual, equalTo(content));

        @SuppressWarnings("rawtypes")
        ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(producer).send(listCaptor.capture());

        List<Message> messages = listCaptor.getValue();
        assertThat(messages.size(), equalTo(1));

        Message actualMessage = messages.get(0);
        verifyMessage(actualMessage, uuid, lastModified, content);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void verifyMessage(Message actualMessage, UUID expectedUuid, Date expectedLastModified, Content expectedContent)
            throws Exception {

        assertThat(actualMessage.getMessageType(), equalTo(CMS_CONTENT_PUBLISHED));
        assertThat(actualMessage.getOriginSystemId(), equalTo(SYSTEM_ID));
        assertThat(actualMessage.getContentType(), equalTo(JSON));
        assertThat(actualMessage.getCustomMessageHeader(TRANSACTION_ID_HEADER), equalTo(PUBLISH_REF));

        Map messageBody = JACKSON_MAPPER.readValue(actualMessage.getMessageBody(), Map.class);
        assertThat(messageBody.get("contentUri"), equalTo("http://www.example.org/content/" + expectedUuid.toString()));
        assertThat(OffsetDateTime.parse((String) messageBody.get("lastModified")).toInstant(), equalTo(expectedLastModified.toInstant()));

        Map actualContent = (Map) messageBody.get("payload");

        assertThat(actualContent.get("uuid"), equalTo(expectedContent.getUuid()));
        assertThat(actualContent.get("title"), equalTo(expectedContent.getTitle()));
        assertThat(actualContent.get("publishReference"), equalTo(expectedContent.getPublishReference()));
        assertThat(OffsetDateTime.parse((String) actualContent.get("lastModified")).toInstant(), equalTo(expectedContent.getLastModified().toInstant()));

        assertThat(actualMessage instanceof KeyedMessage, equalTo(true));
        assertThat(((KeyedMessage) actualMessage).getKey(), equalTo(expectedUuid.toString()));
    }

    @Test(expected = ContentMapperException.class)
    public void thatNoMessageIsSentWhenObjectMapperFails()
            throws Exception {

        ObjectMapper failing = mock(ObjectMapper.class);
        mapper = new MessageProducingContentMapper(delegate, failing, SYSTEM_ID.toString(), producer, URI_BUILDER);

        UUID uuid = UUID.randomUUID();
        Date lastModified = new Date();
        Content content = new Content.Builder()
                .withUuid(uuid)
                .withTitle(TITLE)
                .withPublishReference(PUBLISH_REF)
                .withLastModified(lastModified)
                .build();

        when(delegate.mapImageSet(eq(uuid.toString()), any(EomFile.class), eq(PUBLISH_REF), eq(lastModified))).thenReturn(content);

        when(failing.writeValueAsString(any())).thenThrow(new JsonGenerationException("test exception"));

        try {
            mapper.mapImageSet(uuid.toString(), incoming, PUBLISH_REF, lastModified);
        } finally {
            verifyZeroInteractions(producer);
        }
    }
}
