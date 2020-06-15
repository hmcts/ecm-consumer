package uk.gov.hmcts.ethos.ecm.consumer.servicebus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.primitives.TimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ethos.ecm.consumer.helpers.Helper;
import uk.gov.hmcts.reform.ethos.ecm.consumer.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.ethos.ecm.consumer.exceptions.ServiceBusConnectionTimeoutException;
import uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus.ServiceBusSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@RunWith(SpringJUnit4ClassRunner.class)
public class ServiceBusSenderTest {

    @InjectMocks
    private ServiceBusSender serviceBusSender;
    @Mock
    private IQueueClient sendClient;
    @Mock
    private ObjectMapper objectMapper;

    private UpdateCaseMsg updateCaseMsg;

    @Before
    public void setUp() {
        serviceBusSender = new ServiceBusSender(sendClient, objectMapper);
        updateCaseMsg = Helper.generateUpdateCaseMsg();
    }

    @Test
    public void sendMessageAsync() {
        serviceBusSender.sendMessageAsync(updateCaseMsg);
    }

    @Test
    public void sendMessage() {
        serviceBusSender.sendMessage(updateCaseMsg);
    }

    @Test(expected = InvalidMessageException.class)
    public void sendMessageNull() {
        serviceBusSender.sendMessageAsync(null);
    }

    @Test(expected = InvalidMessageException.class)
    public void sendMessageNullId() {
        updateCaseMsg.setMsgId(null);
        serviceBusSender.sendMessageAsync(updateCaseMsg);
    }

    @Test(expected = ServiceBusConnectionTimeoutException.class)
    public void sendMessageTimeoutException() throws ServiceBusException, InterruptedException {
        doThrow(new TimeoutException()).when(sendClient).send(any());
        serviceBusSender.sendMessage(updateCaseMsg);
    }

        @Test(expected = InvalidMessageException.class)
    public void sendMessageInterruptedException() throws ServiceBusException, InterruptedException {
        doThrow(new InterruptedException()).when(sendClient).send(any());
        serviceBusSender.sendMessage(updateCaseMsg);
    }

    @Test(expected = InvalidMessageException.class)
    public void sendMessageServiceBusException() throws ServiceBusException, InterruptedException {
        doThrow(new ServiceBusException(true)).when(sendClient).send(any());
        serviceBusSender.sendMessage(updateCaseMsg);
    }

}
