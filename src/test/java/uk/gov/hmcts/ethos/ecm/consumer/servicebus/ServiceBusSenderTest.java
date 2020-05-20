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
import uk.gov.hmcts.reform.ethos.ecm.consumer.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.ethos.ecm.consumer.exceptions.ServiceBusConnectionTimeoutException;
import uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus.ServiceBusSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static uk.gov.hmcts.reform.ethos.ecm.consumer.service.MultipleService.CASE_TYPE_ID;
import static uk.gov.hmcts.reform.ethos.ecm.consumer.service.MultipleService.JURISDICTION;

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
        updateCaseMsg = generateMessage();
    }

    @Test
    public void sendMessage() {
        serviceBusSender.sendMessage(generateMessage());
    }

    @Test(expected = InvalidMessageException.class)
    public void sendMessageNull() {
        serviceBusSender.sendMessage(null);
    }

    @Test(expected = InvalidMessageException.class)
    public void sendMessageNullId() {
        updateCaseMsg.setMsgId(null);
        serviceBusSender.sendMessage(updateCaseMsg);
    }

    @Test(expected = InvalidMessageException.class)
    public void sendMessageInterruptedException() throws ServiceBusException, InterruptedException {
        doThrow(new InterruptedException()).when(sendClient).send(any());
        serviceBusSender.sendMessage(updateCaseMsg);
    }

    @Test(expected = ServiceBusConnectionTimeoutException.class)
    public void sendMessageTimeoutException() throws ServiceBusException, InterruptedException {
        doThrow(new TimeoutException()).when(sendClient).send(any());
        serviceBusSender.sendMessage(updateCaseMsg);
    }

    @Test(expected = InvalidMessageException.class)
    public void sendMessageServiceBusException() throws ServiceBusException, InterruptedException {
        doThrow(new ServiceBusException(true)).when(sendClient).send(any());
        serviceBusSender.sendMessage(updateCaseMsg);
    }

    private UpdateCaseMsg generateMessage() {
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

}
