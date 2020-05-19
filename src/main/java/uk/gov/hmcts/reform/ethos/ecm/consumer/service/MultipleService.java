package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.CaseCreationException;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.bulk.SubmitBulkEvent;
import uk.gov.hmcts.reform.ethos.ecm.consumer.tasks.MultipleStateTask;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.*;

@Slf4j
@Service
public class MultipleService {

    private final CcdClient ccdClient;
    private final UserService userService;
    private static final String MESSAGE = "Failed to pull case: ";
    private static final String JURISDICTION = "EMPLOYMENT";
    private static final String CASE_TYPE_ID = SCOTLAND_BULK_CASE_TYPE_ID;
    private static final String MULTIPLE_REFERENCE = "4150001";

    @Value("${caseWorkerUserName}")
    private String caseWorkerUserName;
    @Value("${caseWorkerPassword}")
    private String caseWorkerPassword;

    @Autowired
    public MultipleService(CcdClient ccdClient, UserService userService) {
        this.ccdClient = ccdClient;
        this.userService = userService;
    }

    public void sendUpdateToMultipleLogic() {
        log.info("UserName: " + caseWorkerUserName);
        log.info("Password: " + caseWorkerPassword);
        String accessToken = authenticateUser();
        //String accessToken = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIzamduaWxqOHVpMGJjbXVpNTluZjAxdGRoMiIsInN1YiI6Ijk0MmZmNzJlLWVlZmItNGI5Ni05MzY4LWQ5OGVkMTdlMzMxYyIsImlhdCI6MTU4OTgwMTQxMiwiY2FzZS1pZCI6IjExNTc3MDciLCJldmVudC1pZCI6InVwZGF0ZUJ1bGtBY3Rpb24iLCJjYXNlLXR5cGUtaWQiOiJTY290bGFuZF9NdWx0aXBsZXMiLCJqdXJpc2RpY3Rpb24taWQiOiJFTVBMT1lNRU5UIiwiY2FzZS1zdGF0ZSI6IkJ1bGtDYXNlU3RhcnRlZCIsImNhc2UtdmVyc2lvbiI6IjExNTVmZmMwMTU0YmMzZWNkZjQwNDdhOGM2OTZhMTA1Y2JmZjMxZjUiLCJlbnRpdHktdmVyc2lvbiI6NX0.hL3HN9q2GDnDqdZTc_YewvFH0MWMXfQU84TNfK_Huyo";
        log.info("AccessToken: " + accessToken);
        UserDetails userDetails = userService.getUserDetails(accessToken);
        log.info("UserDetails: " + userDetails);
        List<SubmitBulkEvent> submitBulkEvents = retrieveMultipleCase(accessToken);
        if (submitBulkEvents != null && !submitBulkEvents.isEmpty()) {

            for (SubmitBulkEvent submitBulkEvent : submitBulkEvents) {
                if (submitBulkEvent.getCaseData().getMultipleReference().equals(MULTIPLE_REFERENCE)) {
                    log.info("submit BulkEvent: " + submitBulkEvent);
                    sendUpdate(submitBulkEvent, accessToken);
                }
            }

            //log.info("submit BulkEvent: " + submitBulkEvents.get(0));
            //sendUpdate(submitBulkEvents.get(0), accessToken);
        } else {
            log.info("No submit events found");
        }
    }

    private String authenticateUser() {
        log.info("Login user");
        String accessToken = userService.getAccessToken(caseWorkerUserName, caseWorkerPassword);
        log.info("API ACCESS TOKEN: " + accessToken);
        return accessToken;
    }

    private List<SubmitBulkEvent> retrieveMultipleCase(String authToken) {
        try {
            //return ccdClient.retrieveBulkCasesElasticSearch(authToken, CASE_TYPE_ID, MULTIPLE_REFERENCE);
            return ccdClient.retrieveBulkCases(authToken, CASE_TYPE_ID, JURISDICTION);
        } catch (Exception ex) {
            throw new CaseCreationException(MESSAGE + MULTIPLE_REFERENCE + ex.getMessage());
        }
    }

    private void sendUpdate(SubmitBulkEvent submitBulkEvent, String accessToken) {
        Instant start = Instant.now();
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(new MultipleStateTask(JURISDICTION, CASE_TYPE_ID, submitBulkEvent, accessToken, ccdClient));
        log.info("End in time: " + Duration.between(start, Instant.now()).toMillis());
        executor.shutdown();
    }
}
