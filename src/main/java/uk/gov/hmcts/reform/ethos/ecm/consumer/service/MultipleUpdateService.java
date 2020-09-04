package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.bulk.SubmitBulkEvent;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.MultipleErrors;

import java.io.IOException;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ERRORED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;

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

    public void sendUpdateToMultipleLogic(UpdateCaseMsg updateCaseMsg, List<MultipleErrors> multipleErrorsList) throws IOException {

        String accessToken = userService.getAccessToken();

        List<SubmitBulkEvent> submitBulkEvents = retrieveMultipleCase(accessToken, updateCaseMsg);
        if (submitBulkEvents != null && !submitBulkEvents.isEmpty()) {

//            for (SubmitBulkEvent submitBulkEvent : submitBulkEvents) {
//                if (submitBulkEvent.getCaseData().getMultipleReference().equals(updateCaseMsg.getMultipleRef())) {
//                    log.info("submit BulkEvent: " + submitBulkEvent);
//                    sendUpdate(submitBulkEvent, accessToken, updateCaseMsg);
//                }
//            }

            sendUpdate(submitBulkEvents.get(0), accessToken, updateCaseMsg, multipleErrorsList);

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

    private void sendUpdate(SubmitBulkEvent submitBulkEvent, String accessToken, UpdateCaseMsg updateCaseMsg,
                            List<MultipleErrors> multipleErrorsList) throws IOException {

        String caseTypeId = updateCaseMsg.getCaseTypeId();
        String jurisdiction = updateCaseMsg.getJurisdiction();
        String caseId = String.valueOf(submitBulkEvent.getCaseId());

        CCDRequest returnedRequest = ccdClient.startBulkAmendEventForCase(accessToken,
                                                                     caseTypeId,
                                                                     jurisdiction,
                                                                     caseId);
        log.info("Updating the multiple STATE");

        if (multipleErrorsList != null && !multipleErrorsList.isEmpty()) {

            submitBulkEvent.getCaseData().setState(ERRORED_STATE);

        } else {

            submitBulkEvent.getCaseData().setState(OPEN_STATE);

        }

        ccdClient.submitBulkEventForCase(accessToken,
                                         submitBulkEvent.getCaseData(),
                                         caseTypeId,
                                         jurisdiction,
                                         returnedRequest,
                                         caseId);
    }
}
