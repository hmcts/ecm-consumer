package uk.gov.hmcts.ethos.ecm.consumer.servicebus;


import com.microsoft.azure.servicebus.IQueueClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus.CreateUpdatesMsgCompletor;

import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
public class CreateUpdatesMsgCompletorTest {

    @InjectMocks
    private CreateUpdatesMsgCompletor completor;
    @Mock
    private IQueueClient queueClient;

    private static final UUID LOCK_TOKEN = UUID.randomUUID();

    @Before
    public void setUp() {
        completor = new CreateUpdatesMsgCompletor(queueClient);
    }

    @Test
    public void completeAsync() {
        completor.completeAsync(LOCK_TOKEN);
        Mockito.verify(queueClient).completeAsync(LOCK_TOKEN);
        Mockito.verifyNoMoreInteractions(queueClient);
    }

    @Test
    public void deadLetterAsync() {
        completor.deadLetterAsync(LOCK_TOKEN, "reason", "description");
        Mockito.verify(queueClient).deadLetterAsync(LOCK_TOKEN, "reason", "description");
        Mockito.verifyNoMoreInteractions(queueClient);
    }
}
