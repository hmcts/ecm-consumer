package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.CaseCreationException;
import uk.gov.hmcts.ecm.common.model.bulk.SubmitBulkEvent;
import uk.gov.hmcts.reform.ethos.ecm.consumer.idam.ApiAccessToken;
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
    private static final String USERNAME = "eric.ccdcooper@gmail.com";
    private static final String PASSWORD = "Nagoya0102";
    private static final String MULTIPLE_REFERENCE = "4150015";

    @Value("${caseworker_user_name}")
    private String caseWorkerUserName;
    @Value("${caseworker_password}")
    private String caseWorkerPassword;

    @Autowired
    public MultipleService(CcdClient ccdClient, UserService userService) {
        this.ccdClient = ccdClient;
        this.userService = userService;
    }

    //@Scheduled(fixedRate = 300000)
    public void sendUpdateToMultipleLogic() {
        log.info("Running after 5 minutes");
        log.info("UserName: " + caseWorkerUserName);
        log.info("Password: " + caseWorkerPassword);
        //String authToken = authenticateUser();
        String authToken = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJsZmt2dXNmZ25qOHJsa3BjcDA2ODEzY3E3dCIsInN1YiI6IjY1MDY5MmJiLWNlZmUtNDY2YS1iYThkLTY4NzM3NzE3MzA2NCIsImlhdCI6MTU4OTQzNjYzOCwiY2FzZS1pZCI6IjI3OTU1IiwiZXZlbnQtaWQiOiJhbWVuZENhc2VEZXRhaWxzIiwiY2FzZS10eXBlLWlkIjoiU2NvdGxhbmQiLCJqdXJpc2RpY3Rpb24taWQiOiJFTVBMT1lNRU5UIiwiY2FzZS1zdGF0ZSI6IkFjY2VwdGVkIiwiY2FzZS12ZXJzaW9uIjoiMjA4NjNlM2UzNDJhMDMwZmIzZjg4OGFmNWUyZGRkMDAyMmU1OThkMiIsImVudGl0eS12ZXJzaW9uIjoxMX0.S9mJKrM2v4nYGfizqbauOK0QlY-vo68SP4pM0-r1tq8";
        List<SubmitBulkEvent> submitBulkEvents = retrieveMultipleCase(authToken);
        if (submitBulkEvents != null && !submitBulkEvents.isEmpty()) {

//            for (SubmitBulkEvent submitBulkEvent : submitBulkEvents) {
//                if (submitBulkEvent.getCaseData().getMultipleReference().equals(MULTIPLE_REFERENCE)) {
//                    log.info("submit BulkEvent: " + submitBulkEvent);
//                    sendUpdate(submitBulkEvent, authToken);
//                }
//            }

            log.info("submit BulkEvent: " + submitBulkEvents.get(0));
            sendUpdate(submitBulkEvents.get(0), authToken);
        } else {
            log.info("No submit events found");
        }
    }

    private String authenticateUser() {
        ApiAccessToken apiAccessToken = userService.loginUser(caseWorkerUserName, caseWorkerPassword);
        log.info("API ACCESS TOKEN: " + apiAccessToken);
        return apiAccessToken.getAccessToken();
    }

    private List<SubmitBulkEvent> retrieveMultipleCase(String authToken) {
        try {
            return ccdClient.retrieveBulkCasesElasticSearch(authToken, CASE_TYPE_ID, MULTIPLE_REFERENCE);
            //return ccdClient.retrieveBulkCases(authToken, CASE_TYPE_ID, JURISDICTION);
        } catch (Exception ex) {
            throw new CaseCreationException(MESSAGE + MULTIPLE_REFERENCE + ex.getMessage());
        }
    }

    private void sendUpdate(SubmitBulkEvent submitBulkEvent, String authToken) {
        Instant start = Instant.now();
        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_THREADS);
        executor.execute(new MultipleStateTask(JURISDICTION, CASE_TYPE_ID, submitBulkEvent, authToken, ccdClient));
        log.info("End in time: " + Duration.between(start, Instant.now()).toMillis());
        executor.shutdown();
    }
}
