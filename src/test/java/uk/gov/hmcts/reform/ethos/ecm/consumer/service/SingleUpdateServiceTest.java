package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ecm.common.model.multiples.MultipleData;
import uk.gov.hmcts.ecm.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.reform.ethos.ecm.consumer.helpers.Helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE_CASE_TYPE;

@RunWith(SpringJUnit4ClassRunner.class)
public class SingleUpdateServiceTest {

    @InjectMocks
    private transient SingleUpdateService singleUpdateService;
    @Mock
    private transient CcdClient ccdClient;

    private transient SubmitEvent submitEvent;
    private transient List<SubmitMultipleEvent> submitMultipleEvents;
    private transient UpdateCaseMsg updateCaseMsg;
    private transient String userToken;

    @Before
    public void setUp() {
        submitEvent = new SubmitEvent();
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("4150002/2020");
        caseData.setEcmCaseType(MULTIPLE_CASE_TYPE);
        caseData.setMultipleReference("4150002");
        caseData.setMultipleReferenceLinkMarkUp("MultipleReferenceLinkMarkUp");
        submitEvent.setCaseData(caseData);
        submitEvent.setState(ACCEPTED_STATE);

        SubmitMultipleEvent submitMultipleEvent = new SubmitMultipleEvent();
        MultipleData multipleData = new MultipleData();
        multipleData.setMultipleReference("4150002");
        submitMultipleEvent.setCaseData(multipleData);
        submitMultipleEvent.setCaseId(1649258182799287L);
        submitMultipleEvents = new ArrayList<>(Collections.singletonList(submitMultipleEvent));

        updateCaseMsg = Helper.generateUpdateCaseMsg();
        userToken = "accessToken";
    }

    @Test
    public void sendUpdate() throws IOException {
        when(ccdClient.submitEventForCase(anyString(), any(), anyString(), anyString(), any(), anyString()))
            .thenReturn(submitEvent);
        CCDRequest ccdRequest = new CCDRequest();
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(submitEvent.getCaseData());
        ccdRequest.setCaseDetails(caseDetails);
        when(ccdClient.startEventForCaseAPIRole(anyString(), anyString(), anyString(),
                                                anyString()))
            .thenReturn(ccdRequest);
        singleUpdateService.sendUpdate(submitEvent, userToken, updateCaseMsg);

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
    }

    @Test
    public void sendPreAcceptToSingleLogic() throws IOException {
        updateCaseMsg = Helper.generatePreAcceptCaseMsg();
        when(ccdClient.submitEventForCase(anyString(), any(), anyString(), anyString(), any(), anyString()))
            .thenReturn(submitEvent);
        CCDRequest ccdRequest = new CCDRequest();
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(submitEvent.getCaseData());
        ccdRequest.setCaseDetails(caseDetails);
        when(ccdClient.startEventForCasePreAcceptBulkSingle(anyString(), anyString(), anyString(),
                                                anyString()))
            .thenReturn(ccdRequest);
        singleUpdateService.sendUpdate(submitEvent, userToken, updateCaseMsg);

        verify(ccdClient).startEventForCasePreAcceptBulkSingle(
            eq(userToken),
            eq(UtilHelper.getCaseTypeId(updateCaseMsg.getCaseTypeId())),
            eq(updateCaseMsg.getJurisdiction()),
            any()
        );
        verify(ccdClient).submitEventForCase(eq(userToken),
                                             any(),
                                             eq(UtilHelper.getCaseTypeId(updateCaseMsg.getCaseTypeId())),
                                             eq(updateCaseMsg.getJurisdiction()),
                                             any(),
                                             any());
    }

    @Test
    public void sendDisposeToSingleLogic() throws IOException {
        updateCaseMsg = Helper.generateCloseCaseMsg();
        CCDRequest ccdRequest = new CCDRequest();
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(submitEvent.getCaseData());
        ccdRequest.setCaseDetails(caseDetails);
        when(ccdClient.startDisposeEventForCase(anyString(), anyString(), anyString(),
                                         anyString()))
            .thenReturn(ccdRequest);
        when(ccdClient.submitEventForCase(anyString(), any(), anyString(), anyString(), any(), anyString()))
            .thenReturn(submitEvent);
        singleUpdateService.sendUpdate(submitEvent, userToken, updateCaseMsg);

        verify(ccdClient).startDisposeEventForCase(eq(userToken),
                                                   eq(UtilHelper.getCaseTypeId(updateCaseMsg.getCaseTypeId())),
                                                   eq(updateCaseMsg.getJurisdiction()),
                                                   any());
        verify(ccdClient).submitEventForCase(eq(userToken),
                                             any(),
                                             eq(UtilHelper.getCaseTypeId(updateCaseMsg.getCaseTypeId())),
                                             eq(updateCaseMsg.getJurisdiction()),
                                             any(),
                                             any());
    }

    @Test
    public void updateMultipleReferenceLinkMarkUp() throws IOException {
        submitEvent.getCaseData().setMultipleReferenceLinkMarkUp(null);
        when(ccdClient.submitEventForCase(anyString(), any(), anyString(), anyString(), any(), anyString()))
            .thenReturn(submitEvent);
        CCDRequest ccdRequest = new CCDRequest();
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(submitEvent.getCaseData());
        ccdRequest.setCaseDetails(caseDetails);
        when(ccdClient.startEventForCaseAPIRole(anyString(), anyString(), anyString(),
                                                anyString()))
            .thenReturn(ccdRequest);
        when(ccdClient.retrieveMultipleCasesElasticSearchWithRetries(anyString(), anyString(), anyString()))
            .thenReturn(submitMultipleEvents);
        singleUpdateService.sendUpdate(submitEvent, userToken, updateCaseMsg);

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
        verify(ccdClient).retrieveMultipleCasesElasticSearchWithRetries(eq(userToken),
                                                   eq(updateCaseMsg.getCaseTypeId()),
                                                   any());
        verifyNoMoreInteractions(ccdClient);
    }

}
