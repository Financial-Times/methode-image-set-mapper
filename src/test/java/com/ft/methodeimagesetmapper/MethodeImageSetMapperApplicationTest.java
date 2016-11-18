package com.ft.methodeimagesetmapper;

import com.ft.message.consumer.MessageListener;
import com.ft.messagequeueproducer.MessageProducer;
import com.ft.messaging.standards.message.v1.Message;
import com.ft.messaging.standards.message.v1.SystemId;
import com.ft.methodeimagesetmapper.configuration.ConsumerConfiguration;
import com.ft.methodeimagesetmapper.configuration.MethodeImageSetMapperConfiguration;
import com.ft.methodeimagesetmapper.configuration.ProducerConfiguration;
import com.google.common.io.Files;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.sun.jersey.api.client.Client;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.ft.api.util.transactionid.TransactionIdUtils.TRANSACTION_ID_HEADER;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class MethodeImageSetMapperApplicationTest {

    private static final String CONFIG_FILE = "methode-image-set-mapper-test.yaml";
    private static final String TRANSACTION_ID = "tid_ptvw9xpnhv";

    private static MessageListener consumer;

    private static MessageProducer producer = mock(MessageProducer.class);

    public static class StubMethodeImageModelMapperApplication extends MethodeImageSetMapperApplication {

        @Override
        protected void startListener(Environment environment, MessageListener listener,
                                     ConsumerConfiguration config, Client consumerClient) {
            consumer = listener;
        }

        @Override
        protected MessageProducer configureMessageProducer(Environment environment, ProducerConfiguration config) {
            return producer;
        }
    }

    @ClassRule
    public static DropwizardAppRule<MethodeImageSetMapperConfiguration> appRule =
            new DropwizardAppRule<>(StubMethodeImageModelMapperApplication.class, CONFIG_FILE);

    @SuppressWarnings("unchecked")
    @Test
    public void testMethodeImageModelMessageIsProcessed() throws IOException {
        String messageBody = Files.toString(new File("src/test/resources/native-methode-image.json"), UTF_8);
        SystemId methodeSystemId = SystemId.systemIdFromCode("methode-web-pub");

        Message message = new Message.Builder()
                .withMessageId(UUID.randomUUID())
                .withMessageType("cms-content-published")
                .withOriginSystemId(methodeSystemId)
                .withMessageTimestamp(new Date())
                .withContentType("application/json")
                .withMessageBody(messageBody)
                .build();
        message.addCustomMessageHeader(TRANSACTION_ID_HEADER, TRANSACTION_ID);

        assertThat(consumer.onMessage(message, TRANSACTION_ID), equalTo(true));

        ArgumentCaptor<List> sent = ArgumentCaptor.forClass(List.class);

        verify(producer).send(sent.capture());
        List<Message> sentMessages = sent.getValue();
        assertThat(sentMessages.size(), equalTo(1));
        checkImageSetMessage(sentMessages.get(0), TRANSACTION_ID);
    }

    private void checkImageSetMessage(Message actual, String txId) {
        assertThat(actual.getCustomMessageHeader(TRANSACTION_ID_HEADER), equalTo(txId));

        DocumentContext json = JsonPath.parse(actual.getMessageBody());
        Map jsonPayload = json.read("$.payload");

        String uuid = (String) jsonPayload.get("uuid");
        assertThat(json.read("$.contentUri"), endsWith("/image-set/model/" + uuid));
        assertThat(jsonPayload.get("title"), equalTo("Fruits of the soul"));
        assertThat(jsonPayload.get("description"), equalTo("Picture with fruits"));
        assertThat(jsonPayload.get("mediaType"), equalTo("image/jpeg"));
        List members = (List)jsonPayload.get("members");
        Map firstMember = (Map)members.get(0);
        assertThat(firstMember.get("uuid"), equalTo("d7625378-d4cd-11e2-bce1-002128161462"));
        assertThat(jsonPayload.get("publishReference"), equalTo(txId));
    }

    @Test
    public void testMessageFromInvalidSourceIsDiscarded() throws IOException {
        String messageBody = Files.toString(new File("src/test/resources/native-wp-content.json"), UTF_8);
        SystemId methodeSystemId = SystemId.systemIdFromCode("wordpress");

        Message message = new Message.Builder()
                .withMessageId(UUID.randomUUID())
                .withMessageType("cms-content-published")
                .withOriginSystemId(methodeSystemId)
                .withMessageTimestamp(new Date())
                .withContentType("application/json")
                .withMessageBody(messageBody)
                .build();

        assertThat(consumer.onMessage(message, TRANSACTION_ID), equalTo(true));

        verifyZeroInteractions(producer);
    }

    @Test
    public void testEmptyPayloadMessageIsDiscarded() throws IOException {
        String messageBody = Files.toString(new File("src/test/resources/native-empty-payload-image.json"), UTF_8);
        SystemId methodeSystemId = SystemId.systemIdFromCode("methode-web-pub");

        Message message = new Message.Builder()
                .withMessageId(UUID.randomUUID())
                .withMessageType("cms-content-published")
                .withOriginSystemId(methodeSystemId)
                .withMessageTimestamp(new Date())
                .withContentType("application/json")
                .withMessageBody(messageBody)
                .build();
        message.addCustomMessageHeader(TRANSACTION_ID_HEADER, TRANSACTION_ID);

        assertThat(consumer.onMessage(message, TRANSACTION_ID), equalTo(true));
        verifyZeroInteractions(producer);
    }


}
