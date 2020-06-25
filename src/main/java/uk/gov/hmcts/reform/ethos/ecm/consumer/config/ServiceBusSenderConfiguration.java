package uk.gov.hmcts.reform.ethos.ecm.consumer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.IQueueClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.ecm.common.servicebus.ServiceBusSender;
import uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus.MessageAutoCompletor;

@AutoConfigureAfter(QueueClientConfiguration.class)
@Configuration
public class ServiceBusSenderConfiguration {

    private final ObjectMapper objectMapper;

    public ServiceBusSenderConfiguration(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean(name = "create-updates-completor")
    public MessageAutoCompletor createUpdatesCompletor(
        @Qualifier("create-updates-listen-client") IQueueClient queueClient) {
        return new MessageAutoCompletor(queueClient);
    }

    @Bean(name = "update-case-send-helper")
    public ServiceBusSender updateCaseSendHelper(
        @Qualifier("update-case-send-client") IQueueClient queueClient) {
        return new ServiceBusSender(queueClient, objectMapper);
    }

    @Bean(name = "update-case-completor")
    public MessageAutoCompletor updateCaseCompletor(
        @Qualifier("update-case-listen-client") IQueueClient queueClient) {
        return new MessageAutoCompletor(queueClient);
    }
}
