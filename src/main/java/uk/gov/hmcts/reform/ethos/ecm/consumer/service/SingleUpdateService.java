package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.repository.MultipleErrorsRepository;
import uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus.UpdateCaseMsg;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.*;

@Slf4j
@Service
public class SingleUpdateService {

    private final CcdClient ccdClient;
    private final UserService userService;
    private final MultipleErrorsRepository multipleErrorsRepository;

    @Autowired
    public SingleUpdateService(CcdClient ccdClient,
                               UserService userService,
                               MultipleErrorsRepository multipleErrorsRepository) {
        this.ccdClient = ccdClient;
        this.userService = userService;
        this.multipleErrorsRepository = multipleErrorsRepository;
    }

    public void sendUpdateToSingleLogic(UpdateCaseMsg updateCaseMsg) throws IOException {

        String accessToken = userService.getAccessToken();
        log.info("AccessToken: " + accessToken);

        List<SubmitEvent> submitEvents = retrieveSingleCase(accessToken, updateCaseMsg);
        if (submitEvents != null && !submitEvents.isEmpty()) {

//            for (SubmitEvent submitEvent : submitEvents) {
//                if (submitEvent.getCaseData().getEthosCaseReference().equals(updateCaseMsg.getEthosCaseReference())) {
//                    log.info("submit single Event: " + submitEvent);
//                    sendUpdate(submitEvent, accessToken, updateCaseMsg);
//                }
//            }

            checkStateAndSendUpdate(submitEvents.get(0), accessToken, updateCaseMsg);

        } else {
            log.info("No submit events found");
        }
    }

    private List<SubmitEvent> retrieveSingleCase(String authToken, UpdateCaseMsg updateCaseMsg) throws IOException {

        return ccdClient.retrieveCasesElasticSearch(authToken,
                                                    UtilHelper.getCaseTypeId(updateCaseMsg.getCaseTypeId()),
                                                    new ArrayList<>(Collections.singletonList(updateCaseMsg.getEthosCaseReference())));
//        return ccdClient.retrieveCases(authToken,
//                                       UtilHelper.getCaseTypeId(updateCaseMsg.getCaseTypeId()),
//                                       updateCaseMsg.getJurisdiction());
    }

    private void checkStateAndSendUpdate(SubmitEvent submitEvent, String accessToken, UpdateCaseMsg updateCaseMsg) throws IOException {

        log.info("STATE of submit event: " + submitEvent.getState());

        String validationError = validateCreationSingleCase(submitEvent);

        if (!validationError.isEmpty()) {

            multipleErrorsRepository.persistentQLogMultipleError(updateCaseMsg.getMultipleRef(),
                                                                 updateCaseMsg.getEthosCaseReference(),
                                                                 validationError);

        } else {

            //log.info("submit single Event: " + submitEvent);
            sendUpdate(submitEvent, accessToken, updateCaseMsg);

        }
    }

    private void sendUpdate(SubmitEvent submitEvent, String accessToken, UpdateCaseMsg updateCaseMsg) throws IOException {

        String caseTypeId = UtilHelper.getCaseTypeId(updateCaseMsg.getCaseTypeId());
        String jurisdiction = updateCaseMsg.getJurisdiction();
        String caseId = String.valueOf(submitEvent.getCaseId());

        CCDRequest returnedRequest = ccdClient.startEventForCase(accessToken,
                                                                 caseTypeId,
                                                                 jurisdiction,
                                                                 caseId);
        log.info("Changing multiple ref");
        submitEvent.getCaseData().setState(ACCEPTED_STATE);
        submitEvent.getCaseData().setMultipleReference(updateCaseMsg.getMultipleRef());

//        ccdClient.submitEventForCase(accessToken,
//                                     submitEvent.getCaseData(),
//                                     caseTypeId,
//                                     jurisdiction,
//                                     returnedRequest,
//                                     caseId);
    }

    private String validateCreationSingleCase(SubmitEvent submitEvent) {

        if (!submitEvent.getState().equals(ACCEPTED_STATE)) {

            log.info("ERROR: state of single case not Accepted");
            return "Unprocessable State";

        }

        if (submitEvent.getCaseData().getMultipleReference() != null
            && !submitEvent.getCaseData().getMultipleReference().trim().isEmpty()) {

            log.info("ERROR: already another multiple");
            return "Single Case Taken";

        }

        return "";
    }
}
