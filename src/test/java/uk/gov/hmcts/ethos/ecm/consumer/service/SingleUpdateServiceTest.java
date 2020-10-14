package uk.gov.hmcts.ethos.ecm.consumer.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ethos.ecm.consumer.helpers.Helper;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.SingleUpdateService;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
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

    private List<SubmitEvent> submitEvents;
    private SubmitEvent submitEvent;
    private UpdateCaseMsg updateCaseMsg;
    private String userToken;

    @Before
    public void setUp() {
        submitEvent = new SubmitEvent();
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("4150002/2020");
        submitEvent.setCaseData(caseData);
        submitEvent.setState(ACCEPTED_STATE);
        submitEvents = new ArrayList<>(Collections.singletonList(submitEvent));
        updateCaseMsg = Helper.generateUpdateCaseMsg();
        userToken = "Token";
    }

    @Test
    public void sendUpdateToSingleLogic() throws IOException {
        when(userService.getAccessToken()).thenReturn(userToken);
        when(ccdClient.retrieveCasesElasticSearch(anyString(), anyString(), anyList())).thenReturn(submitEvents);

        when(ccdClient.submitEventForCase(anyString(), any(), anyString(), anyString(), any(), anyString())).thenReturn(submitEvent);
        singleUpdateService.sendUpdateToSingleLogic(updateCaseMsg);

        verifyMocks();
    }

    @Test
    public void sendUpdateToSingleLogicEmptyES() throws IOException {
        when(userService.getAccessToken()).thenReturn(userToken);
        when(ccdClient.retrieveCasesElasticSearch(anyString(), anyString(), anyList())).thenReturn(new ArrayList<>());

        singleUpdateService.sendUpdateToSingleLogic(updateCaseMsg);

        verify(ccdClient).retrieveCasesElasticSearch(eq(userToken),
                                                     eq(UtilHelper.getCaseTypeId(updateCaseMsg.getCaseTypeId())),
                                                     eq(new ArrayList<>(Collections.singletonList(updateCaseMsg.getEthosCaseReference()))));
        verifyNoMoreInteractions(ccdClient);
    }

    @Test
    public void sendUpdateToSingleLogicNullES() throws IOException {
        when(userService.getAccessToken()).thenReturn(userToken);
        when(ccdClient.retrieveCasesElasticSearch(anyString(), anyString(), anyList())).thenReturn(null);

        singleUpdateService.sendUpdateToSingleLogic(updateCaseMsg);

        verify(ccdClient).retrieveCasesElasticSearch(eq(userToken),
                                                     eq(UtilHelper.getCaseTypeId(updateCaseMsg.getCaseTypeId())),
                                                     eq(new ArrayList<>(Collections.singletonList(updateCaseMsg.getEthosCaseReference()))));
        verifyNoMoreInteractions(ccdClient);
    }

    @Test
    public void sendUpdateToSingleLogicUnprocessableState() throws IOException {
        submitEvents.get(0).setState(SUBMITTED_STATE);
        when(userService.getAccessToken()).thenReturn(userToken);
        when(ccdClient.retrieveCasesElasticSearch(anyString(), anyString(), anyList())).thenReturn(submitEvents);

        when(ccdClient.submitEventForCase(anyString(), any(), anyString(), anyString(), any(), anyString())).thenReturn(submitEvent);
        singleUpdateService.sendUpdateToSingleLogic(updateCaseMsg);

        verifyMocks();
    }

    @Test
    public void sendUpdateToSingleLogicWrongState() throws IOException {
        submitEvents.get(0).getCaseData().setMultipleReference("4100001");
        when(userService.getAccessToken()).thenReturn(userToken);
        when(ccdClient.retrieveCasesElasticSearch(anyString(), anyString(), anyList())).thenReturn(submitEvents);

        when(ccdClient.submitEventForCase(anyString(), any(), anyString(), anyString(), any(), anyString())).thenReturn(submitEvent);
        singleUpdateService.sendUpdateToSingleLogic(updateCaseMsg);

        verifyMocks();
    }

    @Test(expected = Exception.class)
    public void sendUpdateToSingleLogicException() throws IOException {
        when(userService.getAccessToken()).thenReturn(userToken);
        when(ccdClient.retrieveCasesElasticSearch(anyString(), anyString(), anyList())).thenThrow(new Exception());

        singleUpdateService.sendUpdateToSingleLogic(updateCaseMsg);

    }

    private void verifyMocks() throws IOException {

        verify(ccdClient).retrieveCasesElasticSearch(eq(userToken),
                                                     eq(UtilHelper.getCaseTypeId(updateCaseMsg.getCaseTypeId())),
                                                     eq(new ArrayList<>(Collections.singletonList(updateCaseMsg.getEthosCaseReference()))));
        verify(ccdClient).startEventForCaseAPIRole(eq(userToken),
                                                   eq(UtilHelper.getCaseTypeId(updateCaseMsg.getCaseTypeId())),
                                                   eq(updateCaseMsg.getJurisdiction()),
                                                   any());
        verify(ccdClient).submitEventForCase(eq(userToken),
                                             any(),
                                             eq(UtilHelper.getCaseTypeId(updateCaseMsg.getCaseTypeId())),
                                             eq(updateCaseMsg.getJurisdiction()),
                                             any(),
                                             any());
        verifyNoMoreInteractions(ccdClient);

    }
}
