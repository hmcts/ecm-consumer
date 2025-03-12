package uk.gov.hmcts.reform.ethos.ecm.consumer.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.UserService;

import java.io.IOException;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class MigrateToReformTaskTest {
    public static final String LEEDS = "Leeds";
    public static final String CASE_ID = "1234567890123456";
    public static final String AUTH_TOKEN = "AuthToken";
    private MigrateToReformTask migrateToReformTask;
    @MockBean
    private UserService userService;
    @MockBean
    private CcdClient ccdClient;

    @BeforeEach
    void setUp() {
        migrateToReformTask = new MigrateToReformTask(userService, ccdClient);
        ReflectionTestUtils.setField(migrateToReformTask, "migrateToReformEnabled", true);
        ReflectionTestUtils.setField(migrateToReformTask, "caseTypeIdsString", LEEDS);
        ReflectionTestUtils.setField(migrateToReformTask, "maxCases", 10);
        ReflectionTestUtils.setField(migrateToReformTask, "threads", 5);
        when(userService.getAccessToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void featureToggleOff() throws IOException {
        ReflectionTestUtils.setField(migrateToReformTask, "migrateToReformEnabled", false);
        migrateToReformTask.migrateToReform();
        verify(userService, times(0)).getAccessToken();
        verify(ccdClient, times(0)).buildAndGetElasticSearchRequest(any(), any(), any());
        verify(ccdClient, times(0)).startEventForCase(any(), any(), any(), any(), any());
        verify(ccdClient, times(0)).submitEventForCase(any(), any(), any(), any(), any(), any());
    }

    @Test
    void migrateToReform() throws IOException {
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("1800001/2020");
        caseData.setClaimant("Claimant");
        caseData.setRespondent("Respondent");

        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseData(caseData);
        submitEvent.setCaseId(1234567890123456L);

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(CASE_ID);
        caseDetails.setCaseData(caseData);
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);

        when(ccdClient.buildAndGetElasticSearchRequest(eq(AUTH_TOKEN), eq(LEEDS), any()))
            .thenReturn(singletonList(submitEvent)).thenReturn(emptyList());
        when(ccdClient.startEventForCase(
            AUTH_TOKEN, LEEDS, "EMPLOYMENT", CASE_ID, "migrateCase")).thenReturn(ccdRequest);
        when(ccdClient.submitEventForCase(
            AUTH_TOKEN, caseData, LEEDS, LEEDS, ccdRequest, CASE_ID
        )).thenReturn(submitEvent);

        migrateToReformTask.migrateToReform();
        verify(ccdClient, times(1)).buildAndGetElasticSearchRequest(eq(AUTH_TOKEN), eq(LEEDS), any());
        verify(ccdClient, times(1)).startEventForCase(
            AUTH_TOKEN, LEEDS, "EMPLOYMENT", CASE_ID, "migrateCase");
        verify(ccdClient, times(1)).submitEventForCase(
            AUTH_TOKEN, caseData, LEEDS, "EMPLOYMENT", ccdRequest, CASE_ID
        );
    }
}
