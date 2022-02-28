package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.bulk.BulkData;
import uk.gov.hmcts.ecm.common.model.bulk.SubmitBulkEvent;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ecm.common.model.multiples.MultipleData;
import uk.gov.hmcts.ecm.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CreationSingleDataModel;
import uk.gov.hmcts.reform.ethos.ecm.consumer.helpers.Helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;

@RunWith(SpringJUnit4ClassRunner.class)
public class SingleUpdateServiceTest {

    @InjectMocks
    private transient SingleUpdateService singleUpdateService;
    @Mock
    private transient CcdClient ccdClient;
    @Mock
    private transient UserService userService;

    private transient SubmitEvent submitEvent;
    private transient UpdateCaseMsg updateCaseMsg;
    private transient String userToken;

    @Before
    public void setUp() {
        submitEvent = new SubmitEvent();
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("4150002/2020");
        submitEvent.setCaseData(caseData);
        submitEvent.setState(ACCEPTED_STATE);
        updateCaseMsg = Helper.generateUpdateCaseMsg();
        userToken = "accessToken";
    }

    @Test
    public void sendUpdate() throws IOException {
        when(ccdClient.submitEventForCase(anyString(), any(), anyString(), anyString(), any(), anyString()))
            .thenReturn(submitEvent);
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
        verifyNoMoreInteractions(ccdClient);
    }

    @Test
    public void sendPreAcceptToSingleLogic() throws IOException {
        updateCaseMsg = Helper.generatePreAcceptCaseMsg();
        when(ccdClient.submitEventForCase(anyString(), any(), anyString(), anyString(), any(), anyString()))
            .thenReturn(submitEvent);
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
        verifyNoMoreInteractions(ccdClient);
    }

    @Test
    public void sendDisposeToSingleLogic() throws IOException {
        updateCaseMsg = Helper.generateCloseCaseMsg();
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
        verifyNoMoreInteractions(ccdClient);
    }

    @Test
    public void updateCreationSingleDataModel() throws IOException {

        updateCaseMsg = Helper.generateCreationSingleCaseMsg();

        MultipleData multipleData = new MultipleData();
        SubmitMultipleEvent submitMultipleEvent = new SubmitMultipleEvent();
        submitMultipleEvent.setCaseData(multipleData);
        List<SubmitMultipleEvent> listSubmitMultipleEvent = new ArrayList<>();
        listSubmitMultipleEvent.add(submitMultipleEvent);
        when(ccdClient.retrieveMultipleCasesElasticSearch(anyString(), anyString(), anyString()))
            .thenReturn(listSubmitMultipleEvent);

        BulkData bulkData = new BulkData();
        SubmitBulkEvent submitBulkEvent = new SubmitBulkEvent();
        submitBulkEvent.setCaseData(bulkData);
        List<SubmitBulkEvent> listSubmitBulkEvent = new ArrayList<>();
        listSubmitBulkEvent.add(submitBulkEvent);
        when(ccdClient.retrieveBulkCasesElasticSearch(anyString(), anyString(), anyString()))
            .thenReturn(listSubmitBulkEvent);

        List<SubmitEvent> listSubmitEvent = new ArrayList<>();
        listSubmitEvent.add(submitEvent);
        when(ccdClient.retrieveCasesElasticSearch(anyString(), anyString(), any()))
            .thenReturn(listSubmitEvent);

        when(userService.getAccessToken()).thenReturn(userToken);

        when(ccdClient.submitEventForCase(anyString(), any(), anyString(), anyString(), any(), anyString()))
            .thenReturn(submitEvent);

        singleUpdateService.updateCreationSingleDataModel(updateCaseMsg);

        String officeCT = (((CreationSingleDataModel) updateCaseMsg.getDataModelParent()).getOfficeCT());

        verify(ccdClient).retrieveMultipleCasesElasticSearch(eq(userToken),
                                                             eq(UtilHelper.getBulkCaseTypeId(officeCT)),
                                                             any());
        verify(ccdClient).retrieveBulkCasesElasticSearch(eq(userToken),
                                                         eq(officeCT),
                                                         any());
        verify(ccdClient).retrieveCasesElasticSearch(eq(userToken),
                                                     eq(officeCT),
                                                     any());
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
