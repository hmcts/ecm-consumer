package uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus;

public enum MessageProcessingResultType {
    SUCCESS,
    UNRECOVERABLE_FAILURE,
    POTENTIALLY_RECOVERABLE_FAILURE
}
