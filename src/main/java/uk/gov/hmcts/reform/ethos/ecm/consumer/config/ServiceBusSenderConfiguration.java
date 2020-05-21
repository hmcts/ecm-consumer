package uk.gov.hmcts.reform.ethos.ecm.consumer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.IQueueClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus.CreateUpdatesMsgCompletor;
import uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus.ServiceBusSender;

@AutoConfigureAfter(QueueClientConfiguration.class)
@Configuration
public class ServiceBusSenderConfiguration {

    private final ObjectMapper objectMapper;

    public ServiceBusSenderConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean(name = "create-updates-send-helper")
    public ServiceBusSender createUpdatesSendHelper(
        @Qualifier("create-updates-send-client") IQueueClient queueClient) {
        return new ServiceBusSender(queueClient, objectMapper);
    }

    @Bean(name = "create-updates-completor")
    public CreateUpdatesMsgCompletor createUpdatesCompletor(
        @Qualifier("create-updates-listen-client") IQueueClient queueClient) {
        return new CreateUpdatesMsgCompletor(queueClient);
    }

    @Bean(name = "update-case-send-helper")
    public ServiceBusSender updateCaseSendHelper(
        @Qualifier("update-case-send-client") IQueueClient queueClient) {
        return new ServiceBusSender(queueClient, objectMapper);
    }

}
