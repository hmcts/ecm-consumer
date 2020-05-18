package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.CaseCreationException;
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
        //String accessToken = "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDVVaTlXVGlvTHQwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJtQG0uY29tIiwiYXV0aF9sZXZlbCI6MCwiYXVkaXRUcmFja2luZ0lkIjoiYjIyMzQ0ZTMtN2NhMS00NmQ5LThmZGUtMjliYmMzMTNiMjNjIiwiaXNzIjoiaHR0cDovL2ZyLWFtOjgwODAvb3BlbmFtL29hdXRoMi9obWN0cyIsInRva2VuTmFtZSI6ImFjY2Vzc190b2tlbiIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJhdXRoR3JhbnRJZCI6ImU1MTVhOTJlLTNjNDAtNDA2ZC05YTAxLTYyMzlkNzhkMzJhNiIsImF1ZCI6ImNjZF9nYXRld2F5IiwibmJmIjoxNTg5Nzg4NTgzLCJncmFudF90eXBlIjoiYXV0aG9yaXphdGlvbl9jb2RlIiwic2NvcGUiOlsib3BlbmlkIiwicHJvZmlsZSIsInJvbGVzIl0sImF1dGhfdGltZSI6MTU4OTc4ODU4MTAwMCwicmVhbG0iOiIvaG1jdHMiLCJleHAiOjE1ODk4MTczODMsImlhdCI6MTU4OTc4ODU4MywiZXhwaXJlc19pbiI6Mjg4MDAsImp0aSI6IjcwMzcwMDlmLTVmMzAtNDNlNC04NDE3LWQ0Yzg1ZTkxZWU5YyJ9.WCtWd_0raTxsZl5FupMySoMjlF5Zp_mg5L-yjGNcEe1PjlayB6guH2DSiPe5i-p40S0yoYD2XDK7eX-NF3INpKxHIB_R4LRLyor7nH-XkeJPCmXFf-X58NMqz-idZ4AOqMDgLtQVjdn4oaSiWQWOkf2AadWfL9RCVFINjiiY3MiZW0EsArgCYW6i17ehsCPky0j8i3dhQXxMqJRrdtZXMQA1cUxDnAOAqvcfyaxU_LYZod7ieW0dLxlfFYbc1OttSgPhQfDX0C7t0lGZAuXFbsW_3XyOhKKtqTriTxQ7jYi3tLXSylkqfchvBCadoGcaYgHHJWhUu0GHfCfOZrxtWQ";
        log.info("AccessToken: " + accessToken);
        List<SubmitBulkEvent> submitBulkEvents = retrieveMultipleCase(accessToken);
        if (submitBulkEvents != null && !submitBulkEvents.isEmpty()) {

//            for (SubmitBulkEvent submitBulkEvent : submitBulkEvents) {
//                if (submitBulkEvent.getCaseData().getMultipleReference().equals(MULTIPLE_REFERENCE)) {
//                    log.info("submit BulkEvent: " + submitBulkEvent);
//                    sendUpdate(submitBulkEvent, accessToken);
//                }
//            }

            log.info("submit BulkEvent: " + submitBulkEvents.get(0));
            sendUpdate(submitBulkEvents.get(0), accessToken);
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
            return ccdClient.retrieveBulkCasesElasticSearch(authToken, CASE_TYPE_ID, MULTIPLE_REFERENCE);
            //return ccdClient.retrieveBulkCases(authToken, CASE_TYPE_ID, JURISDICTION);
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
