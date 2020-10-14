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
import uk.gov.hmcts.ecm.common.exceptions.InvalidMessageException;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.Msg;
import uk.gov.hmcts.ecm.common.servicebus.MessageBodyRetriever;
import uk.gov.hmcts.ecm.common.servicebus.ServiceBusSender;
import uk.gov.hmcts.ethos.ecm.consumer.helpers.Helper;
import uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus.MessageAutoCompletor;
import uk.gov.hmcts.reform.ethos.ecm.consumer.tasks.CreateUpdatesBusReceiverTask;
import java.io.IOException;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class CreateUpdatesBusReceiverTaskTest {

    @InjectMocks
    private CreateUpdatesBusReceiverTask createUpdatesBusReceiverTask;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private MessageAutoCompletor messageCompletor;
    @Mock
    private ServiceBusSender serviceBusSender;

    private Message message;
    private CreateUpdatesMsg msg;

    @Before
    public void setUp() {
        createUpdatesBusReceiverTask = new CreateUpdatesBusReceiverTask(objectMapper, messageCompletor, serviceBusSender);
        msg = Helper.generateCreateUpdatesMsg();
        message = createMessage(msg);
    }

    @Test
    public void onMessageAsync() throws IOException {
        when(objectMapper.readValue(
            MessageBodyRetriever.getBinaryData(message.getMessageBody()),
            CreateUpdatesMsg.class
        )).thenReturn(msg);
        createUpdatesBusReceiverTask.onMessageAsync(message);

        verifyMocks();
    }

    @Test
    public void onMessageAsyncEmptyEthosCaseRefCollection() throws IOException {
        msg.setEthosCaseRefCollection(null);
        message = createMessage(msg);
        when(objectMapper.readValue(
            MessageBodyRetriever.getBinaryData(message.getMessageBody()),
            CreateUpdatesMsg.class
        )).thenReturn(msg);
        createUpdatesBusReceiverTask.onMessageAsync(message);

        verify(objectMapper, times(2)).writeValueAsBytes(msg);
    }

    @Test
    public void onMessageAsyncFailedToParse() throws IOException {
        when(objectMapper.readValue(
            MessageBodyRetriever.getBinaryData(message.getMessageBody()),
            CreateUpdatesMsg.class
        )).thenThrow(new JsonMappingException("Failed to parse"));
        createUpdatesBusReceiverTask.onMessageAsync(message);

        verifyMocks();
    }

    @Test
    public void onMessageAsyncException() throws IOException {
        when(objectMapper.readValue(
            MessageBodyRetriever.getBinaryData(message.getMessageBody()),
            CreateUpdatesMsg.class
        )).thenThrow(new RuntimeException("Failed"));
        createUpdatesBusReceiverTask.onMessageAsync(message);

        verifyMocks();
    }

    private Message createMessage(CreateUpdatesMsg createUpdatesMsg) {
        Message busMessage = new Message();
        busMessage.setContentType("application/json");
        busMessage.setMessageId(createUpdatesMsg.getMsgId());
        busMessage.setMessageBody(getMsgBodyInBytes(createUpdatesMsg));
        busMessage.setLabel(createUpdatesMsg.getJurisdiction());
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

    private void verifyMocks() throws IOException {

        verify(objectMapper).readValue(MessageBodyRetriever.getBinaryData(message.getMessageBody()),
                                       CreateUpdatesMsg.class);
        verify(objectMapper).writeValueAsBytes(msg);

        verifyNoMoreInteractions(objectMapper);

    }

}
