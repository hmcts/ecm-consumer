package uk.gov.hmcts.reform.ethos.ecm.consumer.tasks;

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
import uk.gov.hmcts.ecm.common.exceptions.InvalidMessageException;
import uk.gov.hmcts.ecm.common.model.servicebus.Msg;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.servicebus.MessageBodyRetriever;
import uk.gov.hmcts.reform.ethos.ecm.consumer.helpers.Helper;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.UpdateManagementService;
import uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus.MessageAutoCompletor;

import java.io.IOException;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class UpdateCaseBusReceiverTaskTest {

    @InjectMocks
    private transient UpdateCaseBusReceiverTask updateCaseBusReceiverTask;
    @Mock
    private transient ObjectMapper objectMapper;
    @Mock
    private transient MessageAutoCompletor messageCompletor;
    @Mock
    private transient UpdateManagementService updateManagementService;

    private transient Message message;

    @Before
    public void setUp() {
        updateCaseBusReceiverTask = new UpdateCaseBusReceiverTask(objectMapper,
                                                                  messageCompletor,
                                                                  updateManagementService);
        message = createMessage();
    }

    @Test
    public void onMessageAsync() throws IOException {
        when(objectMapper.readValue(
            MessageBodyRetriever.getBinaryData(message.getMessageBody()),
            UpdateCaseMsg.class
        )).thenReturn(Helper.generateUpdateCaseMsg());
        when(messageCompletor.completeAsync(any())).thenReturn(Helper.getCompletableFuture());
        updateCaseBusReceiverTask.onMessageAsync(message);
    }

    @Test
    public void onMessageAsyncFailedToParse() throws IOException {
        when(objectMapper.readValue(
            MessageBodyRetriever.getBinaryData(message.getMessageBody()),
            UpdateCaseMsg.class
        )).thenThrow(new JsonMappingException("Failed to parse"));
        updateCaseBusReceiverTask.onMessageAsync(message);
    }

    @Test
    public void onMessageAsyncIOException() throws IOException {
        when(objectMapper.readValue(
            MessageBodyRetriever.getBinaryData(message.getMessageBody()),
            UpdateCaseMsg.class
        )).thenThrow(new IOException("Failed"));
        when(messageCompletor.completeAsync(any())).thenReturn(Helper.getCompletableFuture());
        updateCaseBusReceiverTask.onMessageAsync(message);
    }

    @Test
    public void onMessageAsyncException() throws IOException {
        when(objectMapper.readValue(
            MessageBodyRetriever.getBinaryData(message.getMessageBody()),
            UpdateCaseMsg.class
        )).thenThrow(new RuntimeException("Failed"));
        updateCaseBusReceiverTask.onMessageAsync(message);
    }

    @Test
    public void checkIfFinishWhenError() throws IOException, InterruptedException {
        when(objectMapper.readValue(
            MessageBodyRetriever.getBinaryData(message.getMessageBody()),
            UpdateCaseMsg.class
        )).thenReturn(Helper.generateUpdateCaseMsg());
        doThrow(new IOException("Failed")).when(updateManagementService).updateLogic(any());
        updateCaseBusReceiverTask.onMessageAsync(message);
    }

    @Test
    public void checkIfFinishWhenErrorException() throws IOException, InterruptedException {
        doThrow(new IOException("Update logic failed")).when(updateManagementService).updateLogic(any());
        doThrow(new IOException("Check If finish failed")).when(updateManagementService).checkIfFinish(any());
        updateCaseBusReceiverTask.onMessageAsync(message);
    }

    private Message createMessage() {
        UpdateCaseMsg msg = Helper.generateUpdateCaseMsg();
        Message busMessage = new Message();
        busMessage.setContentType("application/json");
        busMessage.setMessageId(msg.getMsgId());
        busMessage.setMessageBody(getMsgBodyInBytes(msg));
        busMessage.setLabel(msg.getJurisdiction());
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

}
