package uk.gov.hmcts.ethos.ecm.consumer.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ethos.ecm.consumer.config.EmailClient;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.MultipleErrors;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.EmailService;
import uk.gov.service.notify.NotificationClientException;

import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.ethos.ecm.consumer.helpers.Constants.*;
import static uk.gov.hmcts.reform.ethos.ecm.consumer.service.EmailService.MULTIPLE_ERRORS;

@RunWith(MockitoJUnitRunner.class)
public class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;
    @Mock
    private EmailClient emailClient;

    private String emailAddress;

    @Before
    public void setUp() {
        emailAddress = "example@hmcts.net";
    }

    @Test
    public void sendConfirmationEmail() throws NotificationClientException {
        emailService.sendConfirmationEmail(emailAddress);
        verify(emailClient).sendEmail(eq(CONFIRMATION_OK_EMAIL), eq(emailAddress), eq(new HashMap<>()), isA(String.class));
        verifyNoMoreInteractions(emailClient);
    }

    @Test
    public void sendConfirmationEmailException() throws NotificationClientException {
        when(emailClient.sendEmail(eq(CONFIRMATION_OK_EMAIL), eq(emailAddress), eq(new HashMap<>()), isA(String.class)))
            .thenThrow(new NotificationClientException("Exception"));
        emailService.sendConfirmationEmail(emailAddress);
        verify(emailClient).sendEmail(eq(CONFIRMATION_OK_EMAIL), eq(emailAddress), eq(new HashMap<>()), isA(String.class));
        verifyNoMoreInteractions(emailClient);
    }

    @Test
    public void sendConfirmationErrorEmail() throws NotificationClientException {
        List<MultipleErrors> multipleErrorsList = generateMultipleErrorList();
        Map<String, String> personalisation = getPersonalisation(multipleErrorsList);
        emailService.sendConfirmationErrorEmail(emailAddress, multipleErrorsList);
        verify(emailClient).sendEmail(eq(CONFIRMATION_ERROR_EMAIL), eq(emailAddress), eq(personalisation), isA(String.class));
        verifyNoMoreInteractions(emailClient);
    }

    private List<MultipleErrors> generateMultipleErrorList() {
        MultipleErrors multipleErrors = new MultipleErrors();
        multipleErrors.setEthoscaseref("4150002/2020");
        multipleErrors.setMultipleref("4150001");
        multipleErrors.setDescription(UNPROCESSABLE_STATE);
        return new ArrayList<>(Collections.singletonList(multipleErrors));
    }

    private Map<String, String> getPersonalisation(List<MultipleErrors> multipleErrorsList) {
        Map<String, String> personalisation = new HashMap<>();
        String errors = multipleErrorsList.stream()
            .map(MultipleErrors::getDescription)
            .collect(Collectors.joining(", "));
        personalisation.put(MULTIPLE_ERRORS, errors);
        return personalisation;
    }

}
