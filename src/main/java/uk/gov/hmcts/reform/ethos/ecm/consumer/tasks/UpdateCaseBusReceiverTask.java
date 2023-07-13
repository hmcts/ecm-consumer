package uk.gov.hmcts.reform.ethos.ecm.consumer.tasks;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.ExceptionPhase;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.exceptions.InvalidMessageException;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.servicebus.MessageBodyRetriever;
import uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus.MessageProcessingResult;
import uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus.MessageProcessingResultType;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.UpdateManagementService;
import uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus.MessageAutoCompletor;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handler of messages for update-case queue.
 */
@DependsOn("update-case-completor")
@Service
@Slf4j
public class UpdateCaseBusReceiverTask implements IMessageHandler {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final int MAX_RETRIES = 3;

    private final transient ObjectMapper objectMapper;
    private final transient MessageAutoCompletor messageCompletor;
    private final transient UpdateManagementService updateManagementService;

    public UpdateCaseBusReceiverTask(ObjectMapper objectMapper,
                                     @Qualifier("update-case-completor") MessageAutoCompletor messageCompletor,
                                     UpdateManagementService updateManagementService) {
        this.objectMapper = objectMapper;
        this.messageCompletor = messageCompletor;
        this.updateManagementService = updateManagementService;
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
                        "An error occurred when trying to handle 'Update Case' message with ID {}",
                        message.getMessageId()
                    );
                }

                return null;
            });
    }

    @Override
    public void notifyException(Throwable throwable, ExceptionPhase exceptionPhase) {
        log.error(
            "An error occurred when handling 'Update Case' message. Phase: {}",
            exceptionPhase,
            throwable
        );
    }

    private CompletableFuture<Void> tryFinaliseMessageAsync(IMessage message,
                                                            MessageProcessingResult processingResult) {
        return finaliseMessageAsync(message, processingResult)
            .exceptionally(updateCaseError -> {
                log.error(
                    "An error occurred when trying to finalise 'Update Case' message with ID {}",
                    message.getMessageId(),
                    updateCaseError
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
                                 log.info("COMPLETED RECEIVED 'Update Case' "
                                              + "----> message with ID {}", message.getMessageId())
                    );
            case UNRECOVERABLE_FAILURE:

                log.info("UNRECOVERABLE FAILURE: Check if finished");
                checkIfFinishWhenError(message);

                return messageCompletor
                    .completeAsync(message.getLockToken())
                    .thenRun(() ->
                                 log.info("UNRECOVERABLE ERROR 'Update Case' "
                                              + "----> message with ID {}", message.getMessageId())
                    );
            default:

                if (message.getDeliveryCount() == MAX_RETRIES) {

                    log.info("RECOVERABLE FAILURE: Last retry checking if finished");
                    checkIfFinishWhenError(message);
                }

                log.info(
                    "Letting 'Update Case' message with ID {} return to the queue. Delivery attempt {}.",
                    message.getMessageId(),
                    message.getDeliveryCount() + 1
                );

                return CompletableFuture.completedFuture(null);
        }
    }

    private void checkIfFinishWhenError(IMessage message) {

        try {

            var updateCaseMsg = readMessage(message);

            log.info("Adding unrecoverable error to database");
            updateManagementService.addUnrecoverableErrorToDatabase(updateCaseMsg);

            log.info("Checking if finished");
            updateManagementService.checkIfFinish(updateCaseMsg);

        } catch (Exception e) {
            log.error("Error reading message when checking if finished", e);
            Thread.currentThread().interrupt();
        }

    }

    private MessageProcessingResult tryProcessMessage(IMessage message) {
        try {

            var updateCaseMsg = readMessage(message);
            log.info("RECEIVED 'Update Case' ------> ethosCaseRef {} - multipleRef {} - multipleRefLinkMarkUp {}",
                     updateCaseMsg.getEthosCaseReference(),
                     updateCaseMsg.getMultipleRef(),
                     updateCaseMsg.getMultipleReferenceLinkMarkUp());

            updateManagementService.updateLogic(updateCaseMsg);

            return new MessageProcessingResult(MessageProcessingResultType.SUCCESS);

        } catch (IOException e) {
            log.error(
                "Unrecoverable error occurred when handling 'Update Case' message with ID {}",
                message.getMessageId(),
                e
            );
            return new MessageProcessingResult(MessageProcessingResultType.UNRECOVERABLE_FAILURE);

        } catch (Exception e) {
            log.error(
                "Potentially recoverable error occurred when handling 'Update Case' message with ID {}",
                message.getMessageId(),
                e
            );
            Thread.currentThread().interrupt();
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
            throw new InvalidMessageException("Failed to parse 'Update Case' message", e);
        }
    }
}
