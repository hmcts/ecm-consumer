package uk.gov.hmcts.ethos.ecm.consumer.tasks;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.MessageBody;
import org.hibernate.sql.Update;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ecm.common.exceptions.InvalidMessageException;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.Msg;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.servicebus.MessageBodyRetriever;
import uk.gov.hmcts.ethos.ecm.consumer.helpers.Helper;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.UpdateManagementService;
import uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus.MessageAutoCompletor;
import uk.gov.hmcts.reform.ethos.ecm.consumer.tasks.UpdateCaseBusReceiverTask;
import java.io.IOException;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class UpdateCaseBusReceiverTaskTest {

    @InjectMocks
    private UpdateCaseBusReceiverTask updateCaseBusReceiverTask;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private MessageAutoCompletor messageCompletor;
    @Mock
    private UpdateManagementService updateManagementService;

    private Message message;

    @Before
    public void setUp() {
        updateCaseBusReceiverTask = new UpdateCaseBusReceiverTask(objectMapper, messageCompletor, updateManagementService);
        message = createMessage();
    }

    @Test
    public void onMessageAsync() throws IOException {
        updateCaseBusReceiverTask.onMessageAsync(message);

        verifyMocks();
    }

    @Test
    public void onMessageAsyncFailedToParse() throws IOException {
        when(objectMapper.readValue(
            MessageBodyRetriever.getBinaryData(message.getMessageBody()),
            UpdateCaseMsg.class
        )).thenThrow(new JsonMappingException("Failed to parse"));
        updateCaseBusReceiverTask.onMessageAsync(message);

        verifyMocks();
    }

    @Test
    public void onMessageAsyncIOException() throws IOException {
        when(objectMapper.readValue(
            MessageBodyRetriever.getBinaryData(message.getMessageBody()),
            UpdateCaseMsg.class
        )).thenThrow(new IOException("Failed"));
        updateCaseBusReceiverTask.onMessageAsync(message);

        verifyMocks();
    }

    @Test
    public void onMessageAsyncException() throws IOException {
        when(objectMapper.readValue(
            MessageBodyRetriever.getBinaryData(message.getMessageBody()),
            UpdateCaseMsg.class
        )).thenThrow(new RuntimeException("Failed"));
        updateCaseBusReceiverTask.onMessageAsync(message);

        verifyMocks();
    }

    @Test
    public void checkIfFinishWhenError() throws IOException {
        doThrow(new IOException("Failed")).when(updateManagementService).updateLogic(any());
        updateCaseBusReceiverTask.onMessageAsync(message);

        verifyMocks();
    }

    @Test
    public void checkIfFinishWhenErrorException() throws IOException {
        doThrow(new IOException("Update logic failed")).when(updateManagementService).updateLogic(any());
        doThrow(new IOException("Check If finish failed")).when(updateManagementService).checkIfFinish(any());
        updateCaseBusReceiverTask.onMessageAsync(message);

        verifyMocks();
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

    private UpdateCaseMsg readMessage(IMessage message) throws IOException {
        try {
            return objectMapper.readValue(
                MessageBodyRetriever.getBinaryData(message.getMessageBody()),
                UpdateCaseMsg.class
            );
        } catch (JsonParseException | JsonMappingException e) {
            throw new InvalidMessageException("Failed to parse 'Update Case' message", e);
        }
    }

    private void verifyMocks() throws IOException {

        verify(objectMapper).readValue(MessageBodyRetriever.getBinaryData(message.getMessageBody()),
                                       UpdateCaseMsg.class);

        verify(objectMapper).writeValueAsBytes(message);

        verifyNoMoreInteractions(objectMapper);

        verify(updateManagementService).updateLogic(readMessage(message));

        verifyNoMoreInteractions(updateManagementService);

    }

}
