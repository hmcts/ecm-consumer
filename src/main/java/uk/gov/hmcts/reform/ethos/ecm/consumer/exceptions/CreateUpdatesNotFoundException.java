package uk.gov.hmcts.reform.ethos.ecm.consumer.exceptions;

public class CreateUpdatesNotFoundException extends RuntimeException {

    public CreateUpdatesNotFoundException(String message) {
        super(message);
    }
}
