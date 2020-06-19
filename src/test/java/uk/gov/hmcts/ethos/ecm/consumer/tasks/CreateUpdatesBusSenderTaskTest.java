package uk.gov.hmcts.ethos.ecm.consumer.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ecm.common.servicebus.ServiceBusSender;
import uk.gov.hmcts.reform.ethos.ecm.consumer.tasks.CreateUpdatesBusSenderTask;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class CreateUpdatesBusSenderTaskTest {

    @InjectMocks
    private CreateUpdatesBusSenderTask createUpdatesBusSenderTask;
    @Mock
    private ServiceBusSender serviceBusSender;

    @Before
    public void setUp() {
        createUpdatesBusSenderTask = new CreateUpdatesBusSenderTask(serviceBusSender);
    }

    //TODO DELETE THIS CLASS
    @Test
    public void runMainMethodTest() {
        createUpdatesBusSenderTask.run();
    }

    @Test
    public void runMainMethodTestException() {
        when(serviceBusSender.sendMessageAsync(any()))
            .thenThrow(new RuntimeException("Failed"));
        createUpdatesBusSenderTask.run();
    }

}
