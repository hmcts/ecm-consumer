package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.reform.ethos.ecm.consumer.helpers.Helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;

@RunWith(SpringJUnit4ClassRunner.class)
public class SingleReadingServiceTest {

    @InjectMocks
    private transient SingleReadingService singleReadingService;
    @Mock
    private transient CcdClient ccdClient;
    @Mock
    private transient UserService userService;
    @Mock
    private transient SingleUpdateService singleUpdateService;
    @Mock
    private transient SingleCreationService singleCreationService;
    @Mock
    private transient SingleTransferService singleTransferService;

    private transient List<SubmitEvent> submitEvents;
    private transient UpdateCaseMsg updateCaseMsg;
    private transient String userToken;

    @Before
    public void setUp() {
        SubmitEvent submitEvent = new SubmitEvent();
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

        singleReadingService.sendUpdateToSingleLogic(updateCaseMsg);
        verify(singleUpdateService).sendUpdate(eq(submitEvents.get(0)),
                                               eq(userToken),
                                               eq(updateCaseMsg));
        verifyNoMoreInteractions(singleUpdateService);
    }

    @Test
    public void sendCreationToSingleLogic() throws IOException {
        updateCaseMsg = Helper.generateCreationSingleCaseMsg();
        when(userService.getAccessToken()).thenReturn(userToken);
        when(ccdClient.retrieveCasesElasticSearch(anyString(), anyString(), anyList())).thenReturn(submitEvents);

        singleReadingService.sendUpdateToSingleLogic(updateCaseMsg);
        verify(singleCreationService).sendCreation(eq(submitEvents.get(0)),
                                               eq(userToken),
                                               eq(updateCaseMsg));
        verifyNoMoreInteractions(singleCreationService);
        verify(singleTransferService).sendTransferred(eq(submitEvents.get(0)),
                                                   eq(userToken),
                                                   eq(updateCaseMsg));
        verifyNoMoreInteractions(singleTransferService);
    }

    @Test
    public void sendUpdateToSingleLogicEmptyCases() throws IOException {
        when(userService.getAccessToken()).thenReturn(userToken);
        when(ccdClient.retrieveCasesElasticSearch(anyString(), anyString(), anyList())).thenReturn(null);

        singleReadingService.sendUpdateToSingleLogic(updateCaseMsg);
        verifyNoMoreInteractions(singleCreationService);
        verifyNoMoreInteractions(singleUpdateService);
    }

}
