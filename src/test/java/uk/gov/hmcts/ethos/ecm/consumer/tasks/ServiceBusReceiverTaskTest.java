package uk.gov.hmcts.ethos.ecm.consumer.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.MessageBody;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.ethos.ecm.consumer.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.ethos.ecm.consumer.exceptions.UpdateCaseNotFoundException;
import uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus.Msg;
import uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus.MessageAutoCompletor;
import uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus.MessageBodyRetriever;
import uk.gov.hmcts.reform.ethos.ecm.consumer.tasks.ServiceBusReceiverTask;

import java.io.IOException;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ethos.ecm.consumer.service.MultipleService.CASE_TYPE_ID;
import static uk.gov.hmcts.reform.ethos.ecm.consumer.service.MultipleService.JURISDICTION;

@RunWith(SpringJUnit4ClassRunner.class)
public class ServiceBusReceiverTaskTest {

    @InjectMocks
    private ServiceBusReceiverTask serviceBusReceiverTask;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private MessageAutoCompletor messageCompletor;

    private Message message;

    @Before
    public void setUp() {
        serviceBusReceiverTask = new ServiceBusReceiverTask(objectMapper, messageCompletor);
        message = createMessage();
    }

    @Test
    public void onMessageAsync() {
        serviceBusReceiverTask.onMessageAsync(message);
    }

    @Test
    public void onMessageAsyncMessageNotFound() throws IOException {
        when(objectMapper.readValue(MessageBodyRetriever.getBinaryData(message.getMessageBody()), UpdateCaseMsg.class))
            .thenThrow(new UpdateCaseNotFoundException("Message not found"));
        serviceBusReceiverTask.onMessageAsync(createMessage());
    }

    @Test
    public void onMessageAsyncInvalidMessage() throws IOException {
        when(objectMapper.readValue(MessageBodyRetriever.getBinaryData(message.getMessageBody()), UpdateCaseMsg.class))
            .thenThrow(new InvalidMessageException("Invalid message"));
        serviceBusReceiverTask.onMessageAsync(createMessage());
    }

    @Test
    public void onMessageAsyncFailedToParse() throws IOException {
        when(objectMapper.readValue(MessageBodyRetriever.getBinaryData(message.getMessageBody()), UpdateCaseMsg.class))
            .thenThrow(new JsonMappingException("Failed to parse"));
        serviceBusReceiverTask.onMessageAsync(createMessage());
    }

    @Test
    public void onMessageAsyncException() throws IOException {
        when(objectMapper.readValue(MessageBodyRetriever.getBinaryData(message.getMessageBody()), UpdateCaseMsg.class))
            .thenThrow(new RuntimeException("Failed to parse"));
        serviceBusReceiverTask.onMessageAsync(createMessage());
    }

    private Message createMessage() {
        UpdateCaseMsg msg = generateMessageContent();
        Message busMessage = new Message();
        busMessage.setContentType("application/json");
        busMessage.setMessageId(msg.getMsgId());
        busMessage.setMessageBody(getMsgBodyInBytes(msg));
        busMessage.setLabel(msg.getLabel());
        return busMessage;
    }

    private UpdateCaseMsg generateMessageContent() {
        return UpdateCaseMsg.builder()
            .msgId("1")
            .multipleRef("4150001")
            .ethosCaseReference("4150001/2020")
            .totalCases("1")
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE_ID)
            .username("eric.ccdcooper@gmail.com")
            .build();
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
}
