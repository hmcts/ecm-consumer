package uk.gov.hmcts.reform.ethos.ecm.consumer.tasks;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import com.microsoft.azure.servicebus.management.QueueRuntimeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ethos.ecm.consumer.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.ethos.ecm.consumer.exceptions.UpdateCaseNotFoundException;
import uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus.MessageProcessingResult;
import uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus.MessageProcessingResultType;
import uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus.MessageAutoCompletor;
import uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus.MessageBodyRetriever;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handler of messages for update-case queue.
 */
@DependsOn({"update-case-completor", "update-case-info-client"})
@Service
@Slf4j
public class UpdateCaseBusReceiverTask implements IMessageHandler {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final ObjectMapper objectMapper;
    private final MessageAutoCompletor messageCompletor;
    private final QueueRuntimeInfo updateCaseInfoClient;

    public UpdateCaseBusReceiverTask(
        ObjectMapper objectMapper,
        @Qualifier("update-case-completor") MessageAutoCompletor messageCompletor,
        @Qualifier("update-case-info-client") QueueRuntimeInfo updateCaseInfoClient) {
        this.objectMapper = objectMapper;
        this.messageCompletor = messageCompletor;
        this.updateCaseInfoClient = updateCaseInfoClient;
    }

    @Override
    public CompletableFuture<Void> onMessageAsync(IMessage message) {
        return CompletableFuture
            .supplyAsync(() -> tryProcessMessage(message), EXECUTOR)
            .thenComposeAsync(processingResult -> tryFinaliseMessageAsync(message, processingResult), EXECUTOR)
            .handleAsync((v, error) -> {
                // Individual steps are supposed to handle their exceptions themselves.
                // This code is here to make sure errors are logged even when they fail to do that.
                if (error != null) {
                    log.error(
                        "An error occurred when trying to handle 'update case' message with ID {}",
                        message.getMessageId()
                    );
                }

                return null;
            });
    }

    @Override
    public void notifyException(Throwable throwable, ExceptionPhase exceptionPhase) {
        log.error(
            "An error occurred when handling 'update case' message. Phase: {}",
            exceptionPhase,
            throwable
        );
    }

    private CompletableFuture<Void> tryFinaliseMessageAsync(IMessage message, MessageProcessingResult processingResult) {
        return finaliseMessageAsync(message, processingResult)
            .exceptionally(error -> {
                log.error(
                    "An error occurred when trying to finalise 'update case' message with ID {}",
                    message.getMessageId(),
                    error
                );

                return null;
            });
    }

    private CompletableFuture<Void> finaliseMessageAsync(IMessage message, MessageProcessingResult processingResult) {
        switch (processingResult.resultType) {
            case SUCCESS:
                return messageCompletor
                    .completeAsync(message.getLockToken())
                    .thenRun(() ->
                        log.info("Completed 'update case' message with ID {}", message.getMessageId())
                    );
            case UNRECOVERABLE_FAILURE:
                return messageCompletor
                    .deadLetterAsync(
                        message.getLockToken(),
                        "Message processing error",
                        processingResult.exception.getMessage()
                    )
                    .thenRun(() ->
                        log.info("Dead-lettered 'update case' message with ID {}", message.getMessageId())
                    );
            default:
                log.info(
                    "Letting 'update case' message with ID {} return to the queue. Delivery attempt {}.",
                    message.getMessageId(),
                    message.getDeliveryCount() + 1
                );

                return CompletableFuture.completedFuture(null);
        }
    }

    private MessageProcessingResult tryProcessMessage(IMessage message) {
        try {
            log.info(
                "Started processing 'update case' message with ID {} (delivery {})",
                message.getMessageId(),
                message.getDeliveryCount() + 1
            );

            UpdateCaseMsg updateCaseMsg = readMessage(message);
            log.info("SEND UPDATE TO THE SINGLE CASE: " + updateCaseMsg);
            getQueueRuntimeInfo();

            log.info("'Update case' message with ID {} processed successfully", message.getMessageId());
            return new MessageProcessingResult(MessageProcessingResultType.SUCCESS);
        } catch (InvalidMessageException e) {
            log.error("Invalid 'update case' message with ID {}", message.getMessageId(), e);
            return new MessageProcessingResult(MessageProcessingResultType.UNRECOVERABLE_FAILURE, e);
        } catch (UpdateCaseNotFoundException e) {
            log.error(
                "Failed to handle 'update case' message with ID {} - message not found",
                message.getMessageId(),
                e
            );
            return new MessageProcessingResult(MessageProcessingResultType.UNRECOVERABLE_FAILURE, e);
        } catch (Exception e) {
            log.error(
                "An error occurred when handling 'update case' message with ID {}",
                message.getMessageId(),
                e
            );
            return new MessageProcessingResult(MessageProcessingResultType.POTENTIALLY_RECOVERABLE_FAILURE);
        }
    }

    private UpdateCaseMsg readMessage(IMessage message) throws IOException {
        try {
            return objectMapper.readValue(
                MessageBodyRetriever.getBinaryData(message.getMessageBody()),
                UpdateCaseMsg.class
            );
        } catch (JsonParseException | JsonMappingException e) {
            throw new InvalidMessageException("Failed to parse 'update case' message", e);
        }
    }

    private void getQueueRuntimeInfo() {
        log.info("RUNTIME INFORMATION --------------> \n " +
                     "Active_messages: " + updateCaseInfoClient.getMessageCountDetails().getActiveMessageCount() +
                     "\nDead Letter messages queue: " + updateCaseInfoClient.getMessageCountDetails().getDeadLetterMessageCount() +
                     "\nMessage count: " + updateCaseInfoClient.getMessageCount() +
                     "\nUpdated At: " + updateCaseInfoClient.getUpdatedAt() +
                     "\nSize of queue: " + updateCaseInfoClient.getSizeInBytes());
    }
}
