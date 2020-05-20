package uk.gov.hmcts.reform.ethos.ecm.consumer.exceptions;

public class UpdateCaseNotFoundException extends RuntimeException {

    public UpdateCaseNotFoundException(String message) {
        super(message);
    }
}
