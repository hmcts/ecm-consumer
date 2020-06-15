package uk.gov.hmcts.ethos.ecm.consumer.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ethos.ecm.consumer.helpers.Helper;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.MultipleErrors;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.repository.MultipleCounterRepository;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.repository.MultipleErrorsRepository;
import uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.MultipleUpdateService;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.SingleUpdateService;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.UpdateManagementService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Mockito.when;

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

    private UpdateCaseMsg updateCaseMsg;

    @Before
    public void setUp() {
        updateCaseMsg = Helper.generateUpdateCaseMsg();
    }

    @Test
    public void updateLogic() throws IOException {
        when(multipleCounterRepository.persistentQGetNextMultipleCountVal(updateCaseMsg.getMultipleRef())).thenReturn(1);
        when(multipleErrorsRepository.findByMultipleRef(updateCaseMsg.getMultipleRef())).thenReturn(new ArrayList<>());

        updateManagementService.updateLogic(updateCaseMsg);
    }

    @Test
    public void updateLogicWithErrors() throws IOException {
        MultipleErrors multipleErrors = new MultipleErrors("4100001", "4100001/2020", "Unprocessable State");
        when(multipleCounterRepository.persistentQGetNextMultipleCountVal(updateCaseMsg.getMultipleRef())).thenReturn(1);
        when(multipleErrorsRepository.findByMultipleRef(updateCaseMsg.getMultipleRef())).thenReturn(new ArrayList<>(
            Collections.singletonList(multipleErrors)));

        updateManagementService.updateLogic(updateCaseMsg);
    }

    @Test
    public void updateLogicWithErrorsDefaultConstructor() throws IOException {
        MultipleErrors multipleErrors = new MultipleErrors();
        when(multipleCounterRepository.persistentQGetNextMultipleCountVal(updateCaseMsg.getMultipleRef())).thenReturn(1);
        when(multipleErrorsRepository.findByMultipleRef(updateCaseMsg.getMultipleRef())).thenReturn(new ArrayList<>(
            Collections.singletonList(multipleErrors)));

        updateManagementService.updateLogic(updateCaseMsg);
    }

}
