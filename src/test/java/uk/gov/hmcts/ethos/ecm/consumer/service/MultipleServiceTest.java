package uk.gov.hmcts.ethos.ecm.consumer.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.bulk.BulkData;
import uk.gov.hmcts.ecm.common.model.bulk.SubmitBulkEvent;
import uk.gov.hmcts.ethos.ecm.consumer.helpers.HelperTest;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.MultipleService;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.UserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringJUnit4ClassRunner.class)
public class MultipleServiceTest {

    @InjectMocks
    private MultipleService multipleService;
    @Mock
    private CcdClient ccdClient;
    @Mock
    private UserService userService;

    private List<SubmitBulkEvent> submitBulkEvents;
    private SubmitBulkEvent submitBulkEvent;

    @Before
    public void setUp() {
        submitBulkEvent = new SubmitBulkEvent();
        BulkData bulkData = new BulkData();
        submitBulkEvent.setCaseData(bulkData);
        submitBulkEvents = new ArrayList<>(Collections.singletonList(submitBulkEvent));
    }

    @Test
    public void sendUpdateToMultipleLogic() throws IOException {
        when(userService.loginUser(any(), any())).thenReturn(HelperTest.getApiAccessToken());
        when(ccdClient.retrieveBulkCasesElasticSearch(anyString(), anyString(), anyString())).thenReturn(submitBulkEvents);
        when(ccdClient.submitBulkEventForCase(anyString(), any(), anyString(), anyString(), any(), anyString())).thenReturn(submitBulkEvent);
        multipleService.sendUpdateToMultipleLogic();
    }

    @Test
    public void sendUpdateToMultipleLogicEmptyES() throws IOException {
        when(userService.loginUser(any(), any())).thenReturn(HelperTest.getApiAccessToken());
        when(ccdClient.retrieveBulkCasesElasticSearch(anyString(), anyString(), anyString())).thenReturn(new ArrayList<>());
        multipleService.sendUpdateToMultipleLogic();
    }

    @Test
    public void sendUpdateToMultipleLogicNullES() throws IOException {
        when(userService.loginUser(any(), any())).thenReturn(HelperTest.getApiAccessToken());
        when(ccdClient.retrieveBulkCasesElasticSearch(anyString(), anyString(), anyString())).thenReturn(null);
        multipleService.sendUpdateToMultipleLogic();
    }

    @Test(expected = Exception.class)
    public void sendUpdateToMultipleLogicException() throws IOException {
        when(userService.loginUser(any(), any())).thenReturn(HelperTest.getApiAccessToken());
        when(ccdClient.retrieveBulkCasesElasticSearch(anyString(), anyString(), anyString())).thenThrow(new RuntimeException());
        multipleService.sendUpdateToMultipleLogic();
    }

    @Test
    public void sendUpdateToMultipleLogicTaskException() throws IOException {
        when(userService.loginUser(any(), any())).thenReturn(HelperTest.getApiAccessToken());
        when(ccdClient.retrieveBulkCasesElasticSearch(anyString(), anyString(), anyString())).thenReturn(submitBulkEvents);
        when(ccdClient.submitBulkEventForCase(anyString(), any(), anyString(), anyString(), any(), anyString())).thenThrow(new IOException());
        multipleService.sendUpdateToMultipleLogic();
    }

}
