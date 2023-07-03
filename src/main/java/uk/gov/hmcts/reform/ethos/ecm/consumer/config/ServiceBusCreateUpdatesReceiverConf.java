package uk.gov.hmcts.reform.ethos.ecm.consumer.config;

import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.servicebus.MessageHandlerOptions;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ethos.ecm.consumer.tasks.CreateUpdatesBusReceiverTask;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;

@AutoConfigureAfter(ServiceBusSenderConfiguration.class)
@Configuration
public class ServiceBusCreateUpdatesReceiverConf {

    @PostConstruct()
    public void registerMessageHandlers() throws InterruptedException, ServiceBusException {
        createUpdatesListenClient.registerMessageHandler(
            createUpdatesBusReceiverTask,
            messageHandlerOptions,
            createUpdatesListenExecutor
        );
    }

    private static final ExecutorService createUpdatesListenExecutor =
        Executors.newSingleThreadExecutor(r ->
                                              new Thread(r, "create-updates-queue-listen")
        );

    private static final MessageHandlerOptions messageHandlerOptions =
        new MessageHandlerOptions(1, false, Duration.ofMinutes(5));

    private final transient IQueueClient createUpdatesListenClient;

    private final transient CreateUpdatesBusReceiverTask createUpdatesBusReceiverTask;

    public ServiceBusCreateUpdatesReceiverConf(
        @Qualifier("create-updates-listen-client") IQueueClient createUpdatesListenClient,
        CreateUpdatesBusReceiverTask createUpdatesBusReceiverTask) {
        this.createUpdatesListenClient = createUpdatesListenClient;
        this.createUpdatesBusReceiverTask = createUpdatesBusReceiverTask;
    }

}
