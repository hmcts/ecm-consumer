package uk.gov.hmcts.ethos.ecm.consumer.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.ResetStateDataModel;
import uk.gov.hmcts.ethos.ecm.consumer.helpers.Helper;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.MultipleErrors;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.repository.MultipleCounterRepository;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.repository.MultipleErrorsRepository;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
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
    private SingleReadingService singleReadingService;
    @Mock
    private EmailService emailService;

    private UpdateCaseMsg updateCaseMsg;

    @Before
    public void setUp() {
        updateCaseMsg = Helper.generateUpdateCaseMsg();
    }

    @Test
    public void updateLogic() throws IOException, InterruptedException {
        when(multipleCounterRepository.persistentQGetNextMultipleCountVal(updateCaseMsg.getMultipleRef())).thenReturn(1);
        when(multipleErrorsRepository.findByMultipleref(updateCaseMsg.getMultipleRef())).thenReturn(new ArrayList<>());

        updateManagementService.updateLogic(updateCaseMsg);

        verify(singleReadingService).sendUpdateToSingleLogic(eq(updateCaseMsg));
        verifyNoMoreInteractions(singleReadingService);
        verify(emailService).sendConfirmationEmail(eq(updateCaseMsg.getUsername()), eq(updateCaseMsg.getMultipleRef()));
        verifyNoMoreInteractions(emailService);
        verify(multipleUpdateService).sendUpdateToMultipleLogic(eq(updateCaseMsg), any());
        verifyNoMoreInteractions(multipleUpdateService);
        verify(multipleCounterRepository).persistentQGetNextMultipleCountVal(eq(updateCaseMsg.getMultipleRef()));
        verify(multipleCounterRepository).deleteAllByMultipleref(eq(updateCaseMsg.getMultipleRef()));
        verify(multipleErrorsRepository).findByMultipleref(eq(updateCaseMsg.getMultipleRef()));
        verify(multipleErrorsRepository).deleteAllByMultipleref(eq(updateCaseMsg.getMultipleRef()));
        verifyNoMoreInteractions(multipleErrorsRepository);
        verifyNoMoreInteractions(multipleCounterRepository);

    }

    @Test
    public void updateLogicWithErrorsDefaultConstructor() throws IOException, InterruptedException {
        MultipleErrors multipleErrors = new MultipleErrors();
        when(multipleCounterRepository.persistentQGetNextMultipleCountVal(updateCaseMsg.getMultipleRef())).thenReturn(1);
        when(multipleErrorsRepository.findByMultipleref(updateCaseMsg.getMultipleRef())).thenReturn(new ArrayList<>(
            Collections.singletonList(multipleErrors)));

        updateManagementService.updateLogic(updateCaseMsg);

        verify(singleReadingService).sendUpdateToSingleLogic(eq(updateCaseMsg));
        verifyNoMoreInteractions(singleReadingService);
        verify(emailService).sendConfirmationErrorEmail(eq(updateCaseMsg.getUsername()),
                                                        eq(new ArrayList<>(Collections.singletonList(multipleErrors))),
                                                        eq(updateCaseMsg.getMultipleRef()));
        verifyNoMoreInteractions(emailService);
        verify(multipleUpdateService).sendUpdateToMultipleLogic(eq(updateCaseMsg), any());
        verifyNoMoreInteractions(multipleUpdateService);
        verify(multipleCounterRepository).persistentQGetNextMultipleCountVal(eq(updateCaseMsg.getMultipleRef()));
        verify(multipleCounterRepository).deleteAllByMultipleref(eq(updateCaseMsg.getMultipleRef()));
        verify(multipleErrorsRepository).findByMultipleref(eq(updateCaseMsg.getMultipleRef()));
        verify(multipleErrorsRepository).deleteAllByMultipleref(eq(updateCaseMsg.getMultipleRef()));
        verifyNoMoreInteractions(multipleErrorsRepository);
        verifyNoMoreInteractions(multipleCounterRepository);

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

    @Test
    public void updateLogicResetState() throws IOException, InterruptedException {

        ResetStateDataModel resetStateDataModel = ResetStateDataModel.builder().build();
        updateCaseMsg.setDataModelParent(resetStateDataModel);
        updateManagementService.updateLogic(updateCaseMsg);

        verify(multipleCounterRepository).deleteAllByMultipleref(eq(updateCaseMsg.getMultipleRef()));
        verify(multipleErrorsRepository).deleteAllByMultipleref(eq(updateCaseMsg.getMultipleRef()));
        verifyNoMoreInteractions(multipleErrorsRepository);
        verifyNoMoreInteractions(multipleCounterRepository);

    }

}
