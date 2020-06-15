package uk.gov.hmcts.ethos.ecm.consumer.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.ecm.consumer.helpers.Helper;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.repository.MultipleErrorsRepository;
import uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.SingleUpdateService;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED_STATE;

@RunWith(SpringJUnit4ClassRunner.class)
public class SingleUpdateServiceTest {

    @InjectMocks
    private SingleUpdateService singleUpdateService;
    @Mock
    private CcdClient ccdClient;
    @Mock
    private UserService userService;
    @Mock
    private MultipleErrorsRepository multipleErrorsRepository;

    private List<SubmitEvent> submitEvents;
    private SubmitEvent submitEvent;
    private UpdateCaseMsg updateCaseMsg;

    @Before
    public void setUp() {
        submitEvent = new SubmitEvent();
        CaseData caseData = new CaseData();
        submitEvent.setCaseData(caseData);
        submitEvent.setState(ACCEPTED_STATE);
        submitEvents = new ArrayList<>(Collections.singletonList(submitEvent));
        updateCaseMsg = Helper.generateUpdateCaseMsg();
    }

    @Test
    public void sendUpdateToSingleLogicLogic() throws IOException {
        when(userService.getAccessToken()).thenReturn("Token");
        when(ccdClient.retrieveCasesElasticSearch(anyString(), anyString(), anyList())).thenReturn(submitEvents);

        //when(ccdClient.retrieveCases(anyString(), anyString(), anyString())).thenReturn(submitEvents);

        when(ccdClient.submitEventForCase(anyString(), any(), anyString(), anyString(), any(), anyString())).thenReturn(submitEvent);
        singleUpdateService.sendUpdateToSingleLogic(updateCaseMsg);
    }

    @Test
    public void sendUpdateToSingleLogicLogicEmptyES() throws IOException {
        when(userService.getAccessToken()).thenReturn("Token");
        when(ccdClient.retrieveCasesElasticSearch(anyString(), anyString(), anyList())).thenReturn(new ArrayList<>());

        //when(ccdClient.retrieveCases(anyString(), anyString(), anyString())).thenReturn(new ArrayList<>());

        singleUpdateService.sendUpdateToSingleLogic(updateCaseMsg);
    }

    @Test
    public void sendUpdateToSingleLogicLogicNullES() throws IOException {
        when(userService.getAccessToken()).thenReturn("Token");
        when(ccdClient.retrieveCasesElasticSearch(anyString(), anyString(), anyList())).thenReturn(null);

        //when(ccdClient.retrieveCases(anyString(), anyString(), anyString())).thenReturn(null);

        singleUpdateService.sendUpdateToSingleLogic(updateCaseMsg);
    }

    @Test
    public void sendUpdateToSingleLogicLogicUnprocessableState() throws IOException {
        submitEvents.get(0).setState(SUBMITTED_STATE);
        when(userService.getAccessToken()).thenReturn("Token");
        when(ccdClient.retrieveCasesElasticSearch(anyString(), anyString(), anyList())).thenReturn(submitEvents);

        //when(ccdClient.retrieveCases(anyString(), anyString(), anyString())).thenReturn(submitEvents);

        when(ccdClient.submitEventForCase(anyString(), any(), anyString(), anyString(), any(), anyString())).thenReturn(submitEvent);
        singleUpdateService.sendUpdateToSingleLogic(updateCaseMsg);
    }

    @Test
    public void sendUpdateToSingleLogicLogicWrongState() throws IOException {
        submitEvents.get(0).getCaseData().setMultipleReference("4100001");
        when(userService.getAccessToken()).thenReturn("Token");
        when(ccdClient.retrieveCasesElasticSearch(anyString(), anyString(), anyList())).thenReturn(submitEvents);

        //when(ccdClient.retrieveCases(anyString(), anyString(), anyString())).thenReturn(submitEvents);

        when(ccdClient.submitEventForCase(anyString(), any(), anyString(), anyString(), any(), anyString())).thenReturn(submitEvent);
        singleUpdateService.sendUpdateToSingleLogic(updateCaseMsg);
    }

    @Test(expected = Exception.class)
    public void sendUpdateToSingleLogicLogicException() throws IOException {
        when(userService.getAccessToken()).thenReturn("Token");
        when(ccdClient.retrieveCasesElasticSearch(anyString(), anyString(), anyList())).thenThrow(new Exception());

        //when(ccdClient.retrieveCases(anyString(), anyString(), anyString())).thenThrow(new Exception());

        singleUpdateService.sendUpdateToSingleLogic(updateCaseMsg);
    }

}
