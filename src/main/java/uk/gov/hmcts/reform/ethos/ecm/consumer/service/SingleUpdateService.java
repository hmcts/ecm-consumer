package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.PreAcceptDataModel;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.RejectDataModel;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Service
public class SingleUpdateService {

    private final CcdClient ccdClient;

    public void sendUpdate(SubmitEvent submitEvent, String accessToken,
                            UpdateCaseMsg updateCaseMsg) throws IOException {

        String caseTypeId = UtilHelper.getCaseTypeId(updateCaseMsg.getCaseTypeId());
        String jurisdiction = updateCaseMsg.getJurisdiction();
        String caseId = String.valueOf(submitEvent.getCaseId());

        CCDRequest returnedRequest = getReturnedRequest(accessToken, caseTypeId,
                                                        jurisdiction, caseId, updateCaseMsg);

        updateCaseMsg.runTask(submitEvent);

        ccdClient.submitEventForCase(accessToken,
                                    submitEvent.getCaseData(),
                                    caseTypeId,
                                    jurisdiction,
                                    returnedRequest,
                                    caseId);

    }

    private CCDRequest getReturnedRequest(String accessToken, String caseTypeId, String jurisdiction,
                                         String caseId, UpdateCaseMsg updateCaseMsg) throws IOException {

        if (updateCaseMsg.getDataModelParent() instanceof PreAcceptDataModel
            || updateCaseMsg.getDataModelParent() instanceof RejectDataModel) {

            return ccdClient.startEventForCasePreAcceptBulkSingle(
                accessToken,
                caseTypeId,
                jurisdiction,
                caseId);

        } else {

            return ccdClient.startEventForCaseAPIRole(
                accessToken,
                caseTypeId,
                jurisdiction,
                caseId);

        }
    }
}
