package uk.gov.hmcts.reform.ethos.ecm.consumer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.IQueueClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus.ServiceBusSender;

@AutoConfigureAfter(QueueClientConfiguration.class)
@Configuration
public class ServiceBusSenderConfiguration {

    @Autowired
    private ObjectMapper objectMapper;

    @Bean(name = "update-case-send-helper")
    public ServiceBusSender updateCaseSendHelper(
        @Qualifier("update-case-send-client") IQueueClient queueClient
    ) {
        return new ServiceBusSender(queueClient, objectMapper);
    }

}
