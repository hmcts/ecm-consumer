package uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus;

import com.microsoft.azure.servicebus.IQueueClient;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class MessageAutoCompletor {

    private final transient IQueueClient queueClient;

    public MessageAutoCompletor(IQueueClient queueClient) {
        this.queueClient = queueClient;
    }

    public CompletableFuture<Void> completeAsync(UUID lockToken) {
        return queueClient.completeAsync(lockToken);
    }

}
