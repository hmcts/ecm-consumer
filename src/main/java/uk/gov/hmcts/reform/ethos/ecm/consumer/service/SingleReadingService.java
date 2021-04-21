package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CreationSingleDataModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;

@Slf4j
@RequiredArgsConstructor
@Service
public class SingleReadingService {

    private final CcdClient ccdClient;
    private final UserService userService;
    private final SingleUpdateService singleUpdateService;
    private final SingleCreationService singleCreationService;
    private final SingleTransferService singleTransferService;

    public void sendUpdateToSingleLogic(UpdateCaseMsg updateCaseMsg) throws IOException {

        String accessToken = userService.getAccessToken();

        List<SubmitEvent> submitEvents = retrieveSingleCase(accessToken, updateCaseMsg);

        if (submitEvents != null && !submitEvents.isEmpty()) {

            if (updateCaseMsg.getDataModelParent() instanceof CreationSingleDataModel) {

                //When single case type then sendTransferred is done in ETHOS-REPL-SERVICE

                if (!updateCaseMsg.getMultipleRef().equals(SINGLE_CASE_TYPE)) {

                    log.info("Send updates to the old case for multiples");

                    singleTransferService.sendTransferred(submitEvents.get(0), accessToken, updateCaseMsg);

                }

                singleCreationService.sendCreation(submitEvents.get(0), accessToken, updateCaseMsg);

            } else {

                singleUpdateService.sendUpdate(submitEvents.get(0), accessToken, updateCaseMsg);

            }

        } else {

            log.info("No submit events found");

        }

    }

    public List<SubmitEvent> retrieveSingleCase(String accessToken, UpdateCaseMsg updateCaseMsg) throws IOException {

        String caseType = !updateCaseMsg.getMultipleRef().equals(SINGLE_CASE_TYPE)
            ? UtilHelper.getCaseTypeId(updateCaseMsg.getCaseTypeId())
            : updateCaseMsg.getCaseTypeId();

        return ccdClient.retrieveCasesElasticSearch(
            accessToken,
            caseType,
            new ArrayList<>(Collections.singletonList(updateCaseMsg.getEthosCaseReference())));

    }

}
