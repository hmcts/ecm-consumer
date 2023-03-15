package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ecm.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CloseDataModel;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.PreAcceptDataModel;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.RejectDataModel;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.UpdateDataModel;
import java.io.IOException;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
@RequiredArgsConstructor
@Service
public class SingleUpdateService {

    private final CcdClient ccdClient;

    @Value("${ccd_gateway_base_url}")
    private String ccdGatewayBaseUrl;

    public void sendUpdate(SubmitEvent submitEvent, String accessToken,
                           UpdateCaseMsg updateCaseMsg) throws IOException {

        var caseTypeId = UtilHelper.getCaseTypeId(updateCaseMsg.getCaseTypeId());
        var jurisdiction = updateCaseMsg.getJurisdiction();
        var caseId = String.valueOf(submitEvent.getCaseId());

        updateMultipleReferenceLinkMarkUp(submitEvent.getCaseData(),
                                          accessToken, updateCaseMsg);
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
        } else if (updateCaseMsg.getDataModelParent() instanceof UpdateDataModel
            && (YES.equals(((UpdateDataModel) updateCaseMsg.getDataModelParent()).getIsRespondentRepRemovalUpdate()))) {
            return ccdClient.startEventForCase(
                accessToken,
                caseTypeId,
                jurisdiction,
                caseId);
        } else if (updateCaseMsg.getDataModelParent() instanceof CloseDataModel) {
            return ccdClient.startDisposeEventForCase(
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

    private void updateMultipleReferenceLinkMarkUp(CaseData caseData, String accessToken,
                                                   UpdateCaseMsg updateCaseMsg) throws IOException {

        if (isNullOrEmpty(caseData.getMultipleReferenceLinkMarkUp())) {
            List<SubmitMultipleEvent> submitMultipleEvents = retrieveMultipleCase(accessToken, updateCaseMsg);
            if (!submitMultipleEvents.isEmpty()) {
                caseData.setMultipleReferenceLinkMarkUp(
                    generateMarkUp(ccdGatewayBaseUrl,
                                   String.valueOf(submitMultipleEvents.get(0).getCaseId()),
                                   caseData.getMultipleReference()));
            }
        }
    }

    private List<SubmitMultipleEvent> retrieveMultipleCase(String authToken,
                                                           UpdateCaseMsg updateCaseMsg) throws IOException {

        return ccdClient.retrieveMultipleCasesElasticSearchWithRetries(
            authToken,
            updateCaseMsg.getCaseTypeId(),
            updateCaseMsg.getMultipleRef());
    }

    private String generateMarkUp(String ccdGatewayBaseUrl, String caseId, String ethosCaseRef) {

        String url = ccdGatewayBaseUrl + "/cases/case-details/" + caseId;
        return "<a target=\"_blank\" href=\"" + url + "\">" + ethosCaseRef + "</a>";
    }

}
