package com.ft.methodeimagesetmapper.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.methodeimagesetmapper.exception.IngesterException;
import com.ft.methodeimagesetmapper.model.EomFile;
import com.ft.methodeimagesetmapper.service.ContentMapper;
import com.ft.methodeimagesetmapper.validation.PublishingValidator;
import com.ft.methodeimagesetmapper.validation.UuidValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import static com.ft.methodeimagesetmapper.util.ImageSetUuidGenerator.fromImageUuid;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NativeCmsPublicationEventsListenerTest {

    private static final String SYSTEM_CODE = "junit";
    private static final String TX_ID = "junittx";
    private static final ObjectMapper JACKSON_MAPPER = new ObjectMapper();
    private static final String UUID = "d7625378-d4cd-11e2-bce1-002128161462";
    private static final String IMAGE_SET_UUID = fromImageUuid(java.util.UUID.fromString(UUID)).toString();

    private NativeCmsPublicationEventsListener listener;

    private NativeCmsPublicationEventsListener errorListener;

    @Mock
    private ContentMapper mapper;

    @Mock
    private UuidValidator uuidValidator;

    @Mock
    private PublishingValidator publishingValidator;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ObjectReader objectReader;

    @Before
    public void setUp() throws IOException {
        listener = new NativeCmsPublicationEventsListener(SYSTEM_CODE, mapper, JACKSON_MAPPER, uuidValidator, publishingValidator);

        when(objectMapper.reader(EomFile.class)).thenReturn(objectReader);
        when(objectReader.readValue(anyString())).thenThrow(IOException.class);
        errorListener = new NativeCmsPublicationEventsListener(SYSTEM_CODE, mapper, objectMapper, uuidValidator, publishingValidator);
    }

    @Test
    public void thatMapperIsCalledWhenMessageIsValid() throws Exception {
        Date lastModified = new Date();
        Message message = new Message();
        message.setOriginSystemId(SystemId.systemIdFromCode(SYSTEM_CODE));
        message.setMessageTimestamp(lastModified);
        message.setMessageBody(JACKSON_MAPPER.writeValueAsString(createSampleMethodeImage()));

        when(publishingValidator.isValidForPublishing(any(EomFile.class))).thenReturn(true);

        assertThat(listener.onMessage(message, TX_ID), is(true));

        ArgumentCaptor<EomFile> c = ArgumentCaptor.forClass(EomFile.class);

        verify(mapper, times(1)).mapImageSet(eq(IMAGE_SET_UUID), c.capture(), eq(TX_ID), eq(message.getMessageTimestamp()));

        EomFile actual = c.getValue();
        assertThat(actual, notNullValue());
        assertThat(actual.getUuid(), equalTo(UUID));

    }

    @Test
    public void thatMapperIsNotCalledWhenMessageHasEmptyBody() throws Exception {
        Date lastModified = new Date();
        Message message = new Message();
        message.setOriginSystemId(SystemId.systemIdFromCode(SYSTEM_CODE));
        message.setMessageTimestamp(lastModified);
        message.setMessageBody(JACKSON_MAPPER.writeValueAsString(createSampleMethodeImage()));

        when(publishingValidator.isValidForPublishing(any(EomFile.class))).thenReturn(false);

        assertThat(listener.onMessage(message, TX_ID), is(true));

        verifyZeroInteractions(mapper);
    }

    @Test
    public void thatMapperIsNotCalledWhenMessageHasNonMatchingSystemCode() {
        Message msg = new Message();
        msg.setOriginSystemId(SystemId.systemIdFromCode("foo"));
        assertThat(listener.onMessage(msg, TX_ID), is(true));
        verifyZeroInteractions(mapper);
    }

    @Test(expected = IngesterException.class)
    public void thatMapperThrowsExceptionWhenMessageCannotBeParsed() throws Exception {
        Date lastModified = new Date();
        Message message = new Message();
        message.setOriginSystemId(SystemId.systemIdFromCode(SYSTEM_CODE));
        message.setMessageTimestamp(lastModified);
        message.setMessageBody(JACKSON_MAPPER.writeValueAsString(createSampleMethodeImage()));
        errorListener.onMessage(message, TX_ID);
    }

    private EomFile createSampleMethodeImage() throws Exception {
        final String attributes = loadFile("sample-attributes.xml");
        final String systemAttributes = loadFile("sample-system-attributes.xml");
        final String usageTickets = loadFile("sample-usage-tickets.xml");
        return new EomFile(UUID, "Image", null, attributes, "", systemAttributes, usageTickets, new Date());
    }

    private String loadFile(final String filename) throws Exception {
        URL resource = getClass().getClassLoader().getResource(filename);
        if (resource != null) {
            final URI uri = resource.toURI();
            return new String(Files.readAllBytes(Paths.get(uri)), "UTF-8");
        }
        return null;
    }
}
