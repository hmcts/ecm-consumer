package uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus;

import com.microsoft.azure.servicebus.IQueueClient;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class MessageAutoCompletor {

    private final IQueueClient queueClient;

    public MessageAutoCompletor(IQueueClient queueClient) {
        this.queueClient = queueClient;
    }

    public CompletableFuture<Void> completeAsync(UUID lockToken) {
        log.info("Complete message");
        return queueClient.completeAsync(lockToken);
    }

    public CompletableFuture<Void> deadLetterAsync(UUID lockToken, String reason, String description) {
        log.info("Move message to Dead Letter");
        return queueClient.deadLetterAsync(lockToken, reason, description);
    }
}
