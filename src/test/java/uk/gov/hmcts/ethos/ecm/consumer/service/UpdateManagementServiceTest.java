package uk.gov.hmcts.ethos.ecm.consumer.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ethos.ecm.consumer.helpers.Helper;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.MultipleErrors;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.repository.MultipleCounterRepository;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.repository.MultipleErrorsRepository;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.EmailService;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.MultipleUpdateService;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.SingleUpdateService;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.UpdateManagementService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.ethos.ecm.consumer.helpers.Constants.CONFIRMATION_OK_EMAIL;
import static uk.gov.hmcts.reform.ethos.ecm.consumer.helpers.Constants.UNPROCESSABLE_MESSAGE;

@RunWith(SpringJUnit4ClassRunner.class)
public class UpdateManagementServiceTest {

    @InjectMocks
    private UpdateManagementService updateManagementService;
    @Mock
    private MultipleCounterRepository multipleCounterRepository;
    @Mock
    private MultipleErrorsRepository multipleErrorsRepository;
    @Mock
    private MultipleUpdateService multipleUpdateService;
    @Mock
    private SingleUpdateService singleUpdateService;
    @Mock
    private EmailService emailService;

    private UpdateCaseMsg updateCaseMsg;

    @Before
    public void setUp() {
        updateCaseMsg = Helper.generateUpdateCaseMsg();
    }

    @Test
    public void updateLogic() throws IOException {
        when(multipleCounterRepository.persistentQGetNextMultipleCountVal(updateCaseMsg.getMultipleRef())).thenReturn(1);
        when(multipleErrorsRepository.findByMultipleref(updateCaseMsg.getMultipleRef())).thenReturn(new ArrayList<>());

        updateManagementService.updateLogic(updateCaseMsg);
    }

    @Test
    public void updateLogicWithErrorsDefaultConstructor() throws IOException {
        MultipleErrors multipleErrors = new MultipleErrors();
        when(multipleCounterRepository.persistentQGetNextMultipleCountVal(updateCaseMsg.getMultipleRef())).thenReturn(1);
        when(multipleErrorsRepository.findByMultipleref(updateCaseMsg.getMultipleRef())).thenReturn(new ArrayList<>(
            Collections.singletonList(multipleErrors)));

        updateManagementService.updateLogic(updateCaseMsg);
    }

    @Test
    public void addUnrecoverableErrorToDatabase() {
        updateManagementService.addUnrecoverableErrorToDatabase(updateCaseMsg);

        verify(multipleErrorsRepository).persistentQLogMultipleError(
            eq(updateCaseMsg.getMultipleRef()),
            eq(updateCaseMsg.getEthosCaseReference()),
            eq(UNPROCESSABLE_MESSAGE));
        verifyNoMoreInteractions(multipleErrorsRepository);

    }

}
