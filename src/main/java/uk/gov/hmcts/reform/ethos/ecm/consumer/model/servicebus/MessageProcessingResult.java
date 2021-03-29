package uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus;

public class MessageProcessingResult {
    public final transient MessageProcessingResultType resultType;
    public final transient Exception exception;

    public MessageProcessingResult(MessageProcessingResultType resultType) {
        this(resultType, null);
    }

    public MessageProcessingResult(MessageProcessingResultType resultType, Exception exception) {
        this.resultType = resultType;
        this.exception = exception;
    }
}
