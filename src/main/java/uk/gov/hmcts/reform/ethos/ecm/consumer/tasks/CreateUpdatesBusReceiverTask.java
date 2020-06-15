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
import uk.gov.hmcts.reform.ethos.ecm.consumer.exceptions.InvalidMessageException;
import uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus.CreateUpdatesMsg;
import uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus.MessageProcessingResult;
import uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus.MessageProcessingResultType;
import uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus.MessageAutoCompletor;
import uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus.MessageBodyRetriever;
import uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus.ServiceBusSender;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handler of messages for create-updates queue.
 */
@DependsOn({"create-updates-completor", "update-case-send-helper"})
@Service
@Slf4j
public class CreateUpdatesBusReceiverTask implements IMessageHandler {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final ObjectMapper objectMapper;
    private final MessageAutoCompletor messageCompletor;
    private final ServiceBusSender serviceBusSender;

    public CreateUpdatesBusReceiverTask(
        ObjectMapper objectMapper,
        @Qualifier("create-updates-completor") MessageAutoCompletor messageCompletor,
        @Qualifier("update-case-send-helper") ServiceBusSender serviceBusSender) {
        this.objectMapper = objectMapper;
        this.messageCompletor = messageCompletor;
        this.serviceBusSender = serviceBusSender;
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
                        "An error occurred when trying to handle 'create updates' message with ID {}",
                        message.getMessageId()
                    );
                }

                return null;
            });
    }

    @Override
    public void notifyException(Throwable throwable, ExceptionPhase exceptionPhase) {
        log.error(
            "An error occurred when handling 'create updates' message. Phase: {}",
            exceptionPhase,
            throwable
        );
    }

    private CompletableFuture<Void> tryFinaliseMessageAsync(IMessage message, MessageProcessingResult processingResult) {
        return finaliseMessageAsync(message, processingResult)
            .exceptionally(error -> {
                log.error(
                    "An error occurred when trying to finalise 'create updates' message with ID {}",
                    message.getMessageId(),
                    error
                );

                return null;
            });
    }

    private CompletableFuture<Void> finaliseMessageAsync(IMessage message, MessageProcessingResult processingResult) {
        if (processingResult.resultType == MessageProcessingResultType.SUCCESS) {
            return messageCompletor
                .completeAsync(message.getLockToken())
                .thenRun(() ->
                             log.info("COMPLETED ----> 'create updates' message with ID {}", message.getMessageId())
                );
        }
        log.info(
            "Letting 'create updates' message with ID {} return to the queue. Delivery attempt {}.",
            message.getMessageId(),
            message.getDeliveryCount() + 1
        );

        return CompletableFuture.completedFuture(null);
    }

    private MessageProcessingResult tryProcessMessage(IMessage message) {
        try {

            CreateUpdatesMsg createUpdatesMsg = readMessage(message);
            sendUpdateCaseMessages(createUpdatesMsg);

            log.info("'Create updates' message with ID {} PROCESSED ------> successfully", message.getMessageId());
            return new MessageProcessingResult(MessageProcessingResultType.SUCCESS);

        } catch (Exception e) {
            log.error(
                "An error occurred when handling 'create updates' message with ID {}",
                message.getMessageId(),
                e
            );
            return new MessageProcessingResult(MessageProcessingResultType.POTENTIALLY_RECOVERABLE_FAILURE);
        }
    }

    private CreateUpdatesMsg readMessage(IMessage message) throws IOException {
        try {
            return objectMapper.readValue(
                MessageBodyRetriever.getBinaryData(message.getMessageBody()),
                CreateUpdatesMsg.class
            );
        } catch (JsonParseException | JsonMappingException e) {
            throw new InvalidMessageException("Failed to parse 'create updates' message", e);
        }
    }

    private void sendUpdateCaseMessages(CreateUpdatesMsg createUpdatesMsg) {
        if (createUpdatesMsg.getEthosCaseRefCollection() != null) {
            for (String ethosCaseReference : createUpdatesMsg.getEthosCaseRefCollection()) {
                UpdateCaseMsg updateCaseMsg = mapToUpdateCaseMsg(createUpdatesMsg, ethosCaseReference);
                serviceBusSender.sendMessageAsync(updateCaseMsg);
            }
        }
    }

    private UpdateCaseMsg mapToUpdateCaseMsg(CreateUpdatesMsg createUpdatesMsg, String ethosCaseReference) {
        return UpdateCaseMsg.builder()
            .msgId(UUID.randomUUID().toString())
            .multipleRef(createUpdatesMsg.getMultipleRef())
            .ethosCaseReference(ethosCaseReference)
            .totalCases(createUpdatesMsg.getTotalCases())
            .jurisdiction(createUpdatesMsg.getJurisdiction())
            .caseTypeId(createUpdatesMsg.getCaseTypeId())
            .username(createUpdatesMsg.getUsername())
            .updateData(createUpdatesMsg.getUpdateData())
            .build();
    }
}
