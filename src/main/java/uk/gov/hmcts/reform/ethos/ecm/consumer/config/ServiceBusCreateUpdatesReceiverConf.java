package uk.gov.hmcts.reform.ethos.ecm.consumer.config;

import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.servicebus.MessageHandlerOptions;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ethos.ecm.consumer.tasks.CreateUpdatesBusReceiverTask;
import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@AutoConfigureAfter(ServiceBusSenderConfiguration.class)
@Configuration
public class ServiceBusCreateUpdatesReceiverConf {

    private static final ExecutorService createUpdatesListenExecutor =
        Executors.newSingleThreadExecutor(r ->
            new Thread(r, "create-updates-queue-listen")
        );

    private static final MessageHandlerOptions messageHandlerOptions =
        new MessageHandlerOptions(10, false, Duration.ofMinutes(5));

    private final IQueueClient createUpdatesListenClient;

    private final CreateUpdatesBusReceiverTask createUpdatesBusReceiverTask;

    public ServiceBusCreateUpdatesReceiverConf(
        @Qualifier("create-updates-listen-client") IQueueClient createUpdatesListenClient,
        CreateUpdatesBusReceiverTask createUpdatesBusReceiverTask) {
        this.createUpdatesListenClient = createUpdatesListenClient;
        this.createUpdatesBusReceiverTask = createUpdatesBusReceiverTask;
    }

    @PostConstruct()
    public void registerMessageHandlers() throws InterruptedException, ServiceBusException {
        createUpdatesListenClient.registerMessageHandler(
            createUpdatesBusReceiverTask,
            messageHandlerOptions,
            createUpdatesListenExecutor
        );
    }
}
