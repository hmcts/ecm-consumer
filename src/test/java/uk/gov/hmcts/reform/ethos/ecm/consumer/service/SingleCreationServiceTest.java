package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.reform.ethos.ecm.consumer.helpers.Helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@RunWith(SpringJUnit4ClassRunner.class)
public class SingleCreationServiceTest {

    @InjectMocks
    private transient SingleCreationService singleCreationService;
    @Mock
    private transient CcdClient ccdClient;
    private transient CCDRequest ccdRequest;
    private transient SubmitEvent submitEvent;
    private transient UpdateCaseMsg updateCaseMsg;
    private transient String userToken;

    @Before
    public void setUp() {
        submitEvent = new SubmitEvent();
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("4150002/2020");
        CasePreAcceptType casePreAcceptType = new CasePreAcceptType();
        casePreAcceptType.setCaseAccepted(YES);
        caseData.setPreAcceptCase(casePreAcceptType);
        submitEvent.setCaseData(caseData);
        submitEvent.setState(ACCEPTED_STATE);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        updateCaseMsg = Helper.generateCreationSingleCaseMsg();
        userToken = "accessToken";
    }

    @Test
    public void sendCreation() throws IOException {
        ccdRequest = new CCDRequest();
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("4450008/2022");
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        ccdRequest.setCaseDetails(caseDetails);
        when(ccdClient.startCaseCreationTransfer(eq(userToken),
                                                 any(uk.gov.hmcts.ecm.common.model.ccd.CaseDetails.class)))
            .thenReturn(ccdRequest);
        when(ccdClient.submitCaseCreation(eq(userToken), any(uk.gov.hmcts.ecm.common.model.ccd.CaseDetails.class),
                                          any())).thenReturn(submitEvent);

        singleCreationService.sendCreation(submitEvent, userToken, updateCaseMsg);

        verify(ccdClient).retrieveCasesElasticSearch(eq(userToken), any(), any());
        verify(ccdClient).startCaseCreationTransfer(eq(userToken),
                                                    any(uk.gov.hmcts.ecm.common.model.ccd.CaseDetails.class));
        verify(ccdClient).submitCaseCreation(eq(userToken),
                                             any(uk.gov.hmcts.ecm.common.model.ccd.CaseDetails.class), any());

        verifyNoMoreInteractions(ccdClient);
    }

    @Test
    public void returnCaseTransfer() throws IOException {
        when(ccdClient.retrieveCasesElasticSearch(anyString(), any(), any()))
            .thenReturn(new ArrayList<>(Collections.singletonList(submitEvent)));
        singleCreationService.sendCreation(submitEvent, userToken, updateCaseMsg);

        verify(ccdClient).retrieveCasesElasticSearch(eq(userToken), any(), any());
        verify(ccdClient).returnCaseCreationTransfer(eq(userToken), anyString(), anyString(), anyString());
        verify(ccdClient).submitEventForCase(eq(userToken), any(), anyString(), anyString(), any(), anyString());
        verifyNoMoreInteractions(ccdClient);
    }
}
