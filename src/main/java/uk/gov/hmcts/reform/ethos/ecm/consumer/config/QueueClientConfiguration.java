package uk.gov.hmcts.reform.ethos.ecm.consumer.config;

import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.servicebus.QueueClient;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueueClientConfiguration {

    @Bean("create-updates-send-client")
    public IQueueClient createUpdatesSendClient(
        @Value("${queue.create-updates.send.connection-string}") String connectionString,
        @Value("${queue.create-updates.queue-name}") String queueName
    ) throws InterruptedException, ServiceBusException {
        return createQueueClient(connectionString, queueName);
    }

    @Bean("create-updates-listen-client")
    public IQueueClient createUpdatesListenClient(
        @Value("${queue.create-updates.listen.connection-string}") String connectionString,
        @Value("${queue.create-updates.queue-name}") String queueName
    ) throws InterruptedException, ServiceBusException {
        return createQueueClient(connectionString, queueName);
    }

    @Bean("update-case-send-client")
    public IQueueClient updateCaseSendClient(
        @Value("${queue.update-case.send.connection-string}") String connectionString,
        @Value("${queue.update-case.queue-name}") String queueName
    ) throws InterruptedException, ServiceBusException {
        return createQueueClient(connectionString, queueName);
    }

    @Bean("update-case-listen-client")
    public IQueueClient updateCaseListenClient(
        @Value("${queue.update-case.listen.connection-string}") String connectionString,
        @Value("${queue.update-case.queue-name}") String queueName
    ) throws InterruptedException, ServiceBusException {
        return createQueueClient(connectionString, queueName);
    }

    private QueueClient createQueueClient(
        String connectionString,
        String queueName
    ) throws ServiceBusException, InterruptedException {
        return new QueueClient(
            new ConnectionStringBuilder(connectionString, queueName),
            ReceiveMode.PEEKLOCK
        );
    }
}
