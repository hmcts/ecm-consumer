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
import uk.gov.hmcts.reform.ethos.ecm.consumer.tasks.UpdateCaseBusReceiverTask;

import java.io.IOException;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.ethos.ecm.consumer.service.MultipleService.CASE_TYPE_ID;
import static uk.gov.hmcts.reform.ethos.ecm.consumer.service.MultipleService.JURISDICTION;

@RunWith(SpringJUnit4ClassRunner.class)
public class UpdateCaseBusReceiverTaskTest {

    @InjectMocks
    private UpdateCaseBusReceiverTask updateCaseBusReceiverTask;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private MessageAutoCompletor messageCompletor;

    private Message message;

    @Before
    public void setUp() {
        updateCaseBusReceiverTask = new UpdateCaseBusReceiverTask(objectMapper, messageCompletor);
        message = createMessage();
    }

    @Test
    public void onMessageAsync() {
        updateCaseBusReceiverTask.onMessageAsync(message);
    }

    @Test
    public void onMessageAsyncMessageNotFound() throws IOException {
        when(objectMapper.readValue(MessageBodyRetriever.getBinaryData(message.getMessageBody()), UpdateCaseMsg.class))
            .thenThrow(new UpdateCaseNotFoundException("Message not found"));
        updateCaseBusReceiverTask.onMessageAsync(createMessage());
    }

    @Test
    public void onMessageAsyncInvalidMessage() throws IOException {
        when(objectMapper.readValue(MessageBodyRetriever.getBinaryData(message.getMessageBody()), UpdateCaseMsg.class))
            .thenThrow(new InvalidMessageException("Invalid message"));
        updateCaseBusReceiverTask.onMessageAsync(createMessage());
    }

    @Test
    public void onMessageAsyncFailedToParse() throws IOException {
        when(objectMapper.readValue(MessageBodyRetriever.getBinaryData(message.getMessageBody()), UpdateCaseMsg.class))
            .thenThrow(new JsonMappingException("Failed to parse"));
        updateCaseBusReceiverTask.onMessageAsync(createMessage());
    }

    @Test
    public void onMessageAsyncException() throws IOException {
        when(objectMapper.readValue(MessageBodyRetriever.getBinaryData(message.getMessageBody()), UpdateCaseMsg.class))
            .thenThrow(new RuntimeException("Failed"));
        updateCaseBusReceiverTask.onMessageAsync(createMessage());
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
