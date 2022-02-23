package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ecm.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CloseDataModel;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CreationSingleDataModel;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.PreAcceptDataModel;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.RejectDataModel;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.UpdateDataModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
@RequiredArgsConstructor
@Service
public class SingleUpdateService {

    private final CcdClient ccdClient;
    private final UserService userService;

    public void sendUpdate(SubmitEvent submitEvent, String accessToken,
                            UpdateCaseMsg updateCaseMsg) throws IOException {

        var caseTypeId = UtilHelper.getCaseTypeId(updateCaseMsg.getCaseTypeId());
        var jurisdiction = updateCaseMsg.getJurisdiction();
        var caseId = String.valueOf(submitEvent.getCaseId());

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

    public void updateCreationSingleDataModel(UpdateCaseMsg updateCaseMsg) throws IOException {

        CreationSingleDataModel creationSingleDataModel =
            ((CreationSingleDataModel) updateCaseMsg.getDataModelParent());
        String caseTypeIdSingle = creationSingleDataModel.getOfficeCT();
        String ccdGatewayBaseUrl = creationSingleDataModel.getCcdGatewayBaseUrl();

        String accessToken = userService.getAccessToken();

        List<SubmitEvent> submitEvents =
            ccdClient.retrieveCasesElasticSearch(
                accessToken,
                caseTypeIdSingle,
                new ArrayList<>(Collections.singletonList(updateCaseMsg.getEthosCaseReference())));

        if (submitEvents != null && !submitEvents.isEmpty()) {

            sendUpdateMultipleReferenceLinkMarkUp(
                submitEvents.get(0), accessToken, caseTypeIdSingle, ccdGatewayBaseUrl, updateCaseMsg);

        }

    }

    private void sendUpdateMultipleReferenceLinkMarkUp(SubmitEvent submitEvent, String accessToken, String caseTypeId,
                                                       String ccdGatewayBaseUrl, UpdateCaseMsg updateCaseMsg)
        throws IOException {

        if (MULTIPLE_CASE_TYPE.equals(submitEvent.getCaseData().getEcmCaseType())) {

            String jurisdiction = updateCaseMsg.getJurisdiction();
            String caseId = String.valueOf(submitEvent.getCaseId());

            List<SubmitMultipleEvent> submitMultipleEvents =
                ccdClient.retrieveMultipleCasesElasticSearch(
                    accessToken,
                    UtilHelper.getBulkCaseTypeId(caseTypeId),
                    submitEvent.getCaseData().getMultipleReference());

            if (!submitMultipleEvents.isEmpty() && submitEvent.getCaseData().getMultipleReferenceLinkMarkUp() == null) {

                submitEvent.getCaseData().setMultipleReferenceLinkMarkUp(
                    generateMarkUp(ccdGatewayBaseUrl,
                                   String.valueOf(submitMultipleEvents.get(0).getCaseId()),
                                   submitEvent.getCaseData().getMultipleReference()));

                CCDRequest returnedRequest = getReturnedRequest(accessToken, caseTypeId,
                                                                jurisdiction, caseId, updateCaseMsg);

                ccdClient.submitEventForCase(accessToken,
                                             submitEvent.getCaseData(),
                                             caseTypeId,
                                             jurisdiction,
                                             returnedRequest,
                                             caseId);

            }

        }

    }

    private String generateMarkUp(String ccdGatewayBaseUrl, String caseId, String ethosCaseRef) {

        String url = ccdGatewayBaseUrl + "/cases/case-details/" + caseId;

        return "<a target=\"_blank\" href=\"" + url + "\">" + ethosCaseRef + "</a>";

    }

}
