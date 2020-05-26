package uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.MessageBody;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.primitives.TimeoutException;
import uk.gov.hmcts.reform.ethos.ecm.consumer.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.ethos.ecm.consumer.exceptions.ServiceBusConnectionTimeoutException;
import uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus.Msg;
import javax.annotation.PreDestroy;

import static java.util.Collections.singletonList;

public class ServiceBusSender implements AutoCloseable {

    private final IQueueClient sendClient;

    private final ObjectMapper objectMapper;

    public ServiceBusSender(IQueueClient queueClient, ObjectMapper objectMapper) {
        this.sendClient = queueClient;
        this.objectMapper = objectMapper;
    }

    public void sendMessage(Msg msg) {
        Message busMessage = mapToBusMessage(msg);
        try {
            sendClient.send(busMessage);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InvalidMessageException("Unable to send message", e);
        } catch (TimeoutException e) {
            throw new ServiceBusConnectionTimeoutException(
                "Service Bus connection timed out while sending the message. Message ID: " + msg.getMsgId(),
                e
            );
        } catch (ServiceBusException e) {
            throw new InvalidMessageException("Unable to send message", e);
        }
    }

    @Override
    public void close() {
        if (sendClient != null) {
            sendClient.closeAsync();
        }
    }

    Message mapToBusMessage(Msg msg) {
        if (msg == null) {
            throw new InvalidMessageException("Msg == null");
        }
        if (Strings.isNullOrEmpty(msg.getMsgId())) {
            throw new InvalidMessageException("Msg Id == null");
        }
        Message busMessage = new Message();
        busMessage.setContentType("application/json");
        busMessage.setMessageId(msg.getMsgId());
        busMessage.setMessageBody(getMsgBodyInBytes(msg));
        busMessage.setLabel(msg.getLabel());

        return busMessage;
    }

    private MessageBody getMsgBodyInBytes(Msg message) {
        try {
            return MessageBody.fromBinaryData(singletonList(
                objectMapper.writeValueAsBytes(message) //default encoding is UTF-8
            ));
        } catch (JsonProcessingException e) {
            throw new InvalidMessageException("Unable to create message body in json format", e);
        }
    }

    @PreDestroy
    public void preDestroy() {
        close();
    }

}
