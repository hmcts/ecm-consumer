package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ethos.ecm.consumer.config.EmailClient;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.MultipleErrors;
import uk.gov.service.notify.NotificationClientException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.ethos.ecm.consumer.helpers.Constants.CONFIRMATION_ERROR_EMAIL;
import static uk.gov.hmcts.reform.ethos.ecm.consumer.helpers.Constants.CONFIRMATION_OK_EMAIL;

@Slf4j
@Service
public class EmailService {

    public static final String MULTIPLE_ERRORS = "multipleErrors";
    public static final String MULTIPLE_REFERENCE = "multipleReference";
    public static final String EMAIL_DESCRIPTION = "send Confirmation email to ";
    public static final String EMAIL_DESCRIPTION_ERROR = "send Confirmation with ERRORS to ";

    private final transient EmailClient emailClient;

    @Autowired
    public EmailService(EmailClient emailClient) {
        this.emailClient = emailClient;
    }

    public void sendConfirmationEmail(String emailAddress, String multipleRef) {
        sendEmail(CONFIRMATION_OK_EMAIL,
                  emailAddress,
                  buildPersonalisation(new ArrayList<>(), multipleRef),
                  EMAIL_DESCRIPTION + emailAddress);
    }

    public void sendConfirmationErrorEmail(String emailAddress,
                                           List<MultipleErrors> multipleErrorsList, String multipleRef) {
        sendEmail(CONFIRMATION_ERROR_EMAIL,
                  emailAddress,
                  buildPersonalisation(multipleErrorsList, multipleRef),
                  EMAIL_DESCRIPTION_ERROR + emailAddress);
    }

    private Map<String, String> buildPersonalisation(List<MultipleErrors> multipleErrorsList, String multipleRef) {
        Map<String, String> personalisation = new HashMap<>();

        String errors = multipleErrorsList.stream()
            .map(MultipleErrors::toString)
            .collect(Collectors.joining(System.lineSeparator()));
        log.info("Sending email for multipleRef: " + multipleRef + " with errors: " + errors);
        personalisation.put(MULTIPLE_ERRORS, errors);
        personalisation.put(MULTIPLE_REFERENCE, multipleRef);

        return personalisation;
    }

    private void sendEmail(String templateId, String emailAddress,
                           Map<String, String> personalisation, String emailDescription) {

        String referenceId = UUID.randomUUID().toString();

        try {
            log.info("Attempting to send {} email. Reference ID: {}", emailDescription, referenceId);
            emailClient.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                referenceId
            );
            log.info("Sending email success. Reference ID: {}", referenceId);

        } catch (NotificationClientException e) {
            log.warn("Failed to send email. Reference ID: {}. Reason:", referenceId, e);
        }
    }
}
