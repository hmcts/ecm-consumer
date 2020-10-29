package uk.gov.hmcts.reform.ethos.ecm.consumer.config;

import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.servicebus.MessageHandlerOptions;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ethos.ecm.consumer.tasks.UpdateCaseBusReceiverTask;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@AutoConfigureAfter(ServiceBusCreateUpdatesReceiverConf.class)
@Configuration
public class ServiceBusUpdateCaseReceiverConf {

    private static final ExecutorService updateCaseListenExecutor =
        Executors.newSingleThreadExecutor(r ->
            new Thread(r, "update-case-queue-listen")
        );

    private static final MessageHandlerOptions messageHandlerOptions =
        new MessageHandlerOptions(1, false, Duration.ofMinutes(5));

    private final IQueueClient updateCaseListenClient;

    private final UpdateCaseBusReceiverTask updateCaseBusReceiverTask;

    public ServiceBusUpdateCaseReceiverConf(
        @Qualifier("update-case-listen-client") IQueueClient updateCaseListenClient,
        UpdateCaseBusReceiverTask updateCaseBusReceiverTask) {
        this.updateCaseListenClient = updateCaseListenClient;
        this.updateCaseBusReceiverTask = updateCaseBusReceiverTask;
    }

    @PostConstruct()
    public void registerMessageHandlers() throws InterruptedException, ServiceBusException {
        updateCaseListenClient.registerMessageHandler(
            updateCaseBusReceiverTask,
            messageHandlerOptions,
            updateCaseListenExecutor
        );
    }
}
