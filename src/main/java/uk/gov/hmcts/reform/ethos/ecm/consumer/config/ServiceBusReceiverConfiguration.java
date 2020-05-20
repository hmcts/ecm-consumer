package uk.gov.hmcts.reform.ethos.ecm.consumer.config;

import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.servicebus.MessageHandlerOptions;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ethos.ecm.consumer.tasks.ServiceBusReceiverTask;
import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@AutoConfigureAfter(ServiceBusSenderConfiguration.class)
@Configuration
public class ServiceBusReceiverConfiguration {

    private static final ExecutorService updateCaseListenExecutor =
        Executors.newSingleThreadExecutor(r ->
            new Thread(r, "create-updates-queue-listen")
        );

    private static final MessageHandlerOptions messageHandlerOptions =
        new MessageHandlerOptions(1, false, Duration.ofMinutes(5));

    private final IQueueClient updateCaseListenClient;

    private final ServiceBusReceiverTask serviceBusReceiverTask;

    public ServiceBusReceiverConfiguration(
        @Qualifier("create-updates-listen-client") IQueueClient updateCaseListenClient,
        ServiceBusReceiverTask serviceBusReceiverTask) {
        this.updateCaseListenClient = updateCaseListenClient;
        this.serviceBusReceiverTask = serviceBusReceiverTask;
    }

    @PostConstruct()
    public void registerMessageHandlers() throws InterruptedException, ServiceBusException {
        updateCaseListenClient.registerMessageHandler(
            serviceBusReceiverTask,
            messageHandlerOptions,
            updateCaseListenExecutor
        );
    }
}
