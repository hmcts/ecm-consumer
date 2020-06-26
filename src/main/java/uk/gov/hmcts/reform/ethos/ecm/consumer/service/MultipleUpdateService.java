package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.bulk.SubmitBulkEvent;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class MultipleUpdateService {

    private final CcdClient ccdClient;
    private final UserService userService;

    @Autowired
    public MultipleUpdateService(CcdClient ccdClient, UserService userService) {
        this.ccdClient = ccdClient;
        this.userService = userService;
    }

    public void sendUpdateToMultipleLogic(UpdateCaseMsg updateCaseMsg) throws IOException {

        String accessToken = userService.getAccessToken();

        List<SubmitBulkEvent> submitBulkEvents = retrieveMultipleCase(accessToken, updateCaseMsg);
        if (submitBulkEvents != null && !submitBulkEvents.isEmpty()) {

//            for (SubmitBulkEvent submitBulkEvent : submitBulkEvents) {
//                if (submitBulkEvent.getCaseData().getMultipleReference().equals(updateCaseMsg.getMultipleRef())) {
//                    log.info("submit BulkEvent: " + submitBulkEvent);
//                    sendUpdate(submitBulkEvent, accessToken, updateCaseMsg);
//                }
//            }

            sendUpdate(submitBulkEvents.get(0), accessToken, updateCaseMsg);

        } else {
            log.info("No submit events found");

        }
    }

    private List<SubmitBulkEvent> retrieveMultipleCase(String authToken, UpdateCaseMsg updateCaseMsg) throws IOException {

        return ccdClient.retrieveBulkCasesElasticSearch(authToken,
                                                        updateCaseMsg.getCaseTypeId(),
                                                        updateCaseMsg.getMultipleRef());
//        return ccdClient.retrieveBulkCases(authToken,
//                                           updateCaseMsg.getCaseTypeId(),
//                                           updateCaseMsg.getJurisdiction());
    }

    private void sendUpdate(SubmitBulkEvent submitBulkEvent, String accessToken, UpdateCaseMsg updateCaseMsg) throws IOException {

        String caseTypeId = updateCaseMsg.getCaseTypeId();
        String jurisdiction = updateCaseMsg.getJurisdiction();
        String caseId = String.valueOf(submitBulkEvent.getCaseId());

        CCDRequest returnedRequest = ccdClient.startBulkEventForCase(accessToken,
                                                                     caseTypeId,
                                                                     jurisdiction,
                                                                     caseId);
        log.info("Updating the multiple STATE");
        submitBulkEvent.getCaseData().setBulkCaseTitle("NAME CHANGED");

        ccdClient.submitBulkEventForCase(accessToken,
                                         submitBulkEvent.getCaseData(),
                                         caseTypeId,
                                         jurisdiction,
                                         returnedRequest,
                                         caseId);
    }
}
