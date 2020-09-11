package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.multiples.SubmitMultipleEvent;
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

        List<SubmitMultipleEvent> submitMultipleEvents = retrieveMultipleCase(accessToken, updateCaseMsg);
        if (submitMultipleEvents != null && !submitMultipleEvents.isEmpty()) {

            sendUpdate(submitMultipleEvents.get(0), accessToken, updateCaseMsg, multipleErrorsList);

        } else {
            log.info("No submit events found");

        }
    }

    private List<SubmitMultipleEvent> retrieveMultipleCase(String authToken, UpdateCaseMsg updateCaseMsg) throws IOException {

        return ccdClient.retrieveMultipleCasesElasticSearchWithRetries(authToken,
                                                        updateCaseMsg.getCaseTypeId(),
                                                        updateCaseMsg.getMultipleRef());
    }

    private void sendUpdate(SubmitMultipleEvent submitMultipleEvent, String accessToken, UpdateCaseMsg updateCaseMsg,
                            List<MultipleErrors> multipleErrorsList) throws IOException {

        String caseTypeId = updateCaseMsg.getCaseTypeId();
        String jurisdiction = updateCaseMsg.getJurisdiction();
        String caseId = String.valueOf(submitMultipleEvent.getCaseId());

        CCDRequest returnedRequest = ccdClient.startBulkAmendEventForCase(accessToken,
                                                                     caseTypeId,
                                                                     jurisdiction,
                                                                     caseId);

        if (multipleErrorsList != null && !multipleErrorsList.isEmpty()) {

            submitMultipleEvent.getCaseData().setState(ERRORED_STATE);

            log.info("Updating the multiple STATE: " + ERRORED_STATE);

        } else {

            submitMultipleEvent.getCaseData().setState(OPEN_STATE);

            log.info("Updating the multiple STATE: " + OPEN_STATE);
        }

        ccdClient.submitMultipleEventForCase(accessToken,
                                         submitMultipleEvent.getCaseData(),
                                         caseTypeId,
                                         jurisdiction,
                                         returnedRequest,
                                         caseId);
    }
}
