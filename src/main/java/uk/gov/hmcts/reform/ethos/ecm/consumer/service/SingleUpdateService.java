package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.bulk.SubmitBulkEvent;
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
import java.util.List;
import java.util.stream.Collectors;

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

    public void updateCreationSingleDataModel(UpdateCaseMsg updateCaseMsg) throws IOException, InterruptedException {

        CreationSingleDataModel creationSingleDataModel =
            ((CreationSingleDataModel) updateCaseMsg.getDataModelParent());
        String caseTypeId = creationSingleDataModel.getOfficeCT();
        String ccdGatewayBaseUrl = creationSingleDataModel.getCcdGatewayBaseUrl();

        String multipleReference = updateCaseMsg.getMultipleRef();

        String accessToken = userService.getAccessToken();

        List<SubmitMultipleEvent> listSubmitMultipleEvent =
            retrieveMultipleCasesWithSleep(
                accessToken,
                UtilHelper.getBulkCaseTypeId(caseTypeId),
                multipleReference);

        if (!listSubmitMultipleEvent.isEmpty()) {

            String generateMarkUp =
                generateMarkUp(
                    ccdGatewayBaseUrl,
                    String.valueOf(listSubmitMultipleEvent.get(0).getCaseId()),
                    multipleReference);

            List<SubmitEvent> listSubmitEvent =
                ccdClient.retrieveCasesElasticSearch(
                    accessToken,
                    caseTypeId,
                    retrieveBulkCasesEthosCaseReference(accessToken, caseTypeId, updateCaseMsg.getMultipleRef()));

            if (listSubmitEvent != null && !listSubmitEvent.isEmpty()) {

                for (SubmitEvent submitEvent : listSubmitEvent) {

                    submitEvent.getCaseData().setMultipleReferenceLinkMarkUp(generateMarkUp);

                    sendUpdate(submitEvent, accessToken, updateCaseMsg);

                }

            }

        }

    }

    private List<SubmitMultipleEvent> retrieveMultipleCasesWithSleep(String accessToken, String caseTypeId,
                                                                         String multipleReference)
        throws IOException, InterruptedException {

        List<SubmitMultipleEvent> listSubmitMultipleEvent =
            ccdClient.retrieveMultipleCasesElasticSearch(
                accessToken,
                caseTypeId,
                multipleReference);

        if (!listSubmitMultipleEvent.isEmpty()) {

            return listSubmitMultipleEvent;

        } else {

            Thread.sleep(3000);

            return ccdClient.retrieveMultipleCasesElasticSearch(
                accessToken,
                caseTypeId,
                multipleReference);

        }

    }

    private List<String> retrieveBulkCasesEthosCaseReference(String accessToken, String caseTypeId,
                                                             String multipleRef) throws IOException {

        List<SubmitBulkEvent> submitBulkEvents =
            ccdClient.retrieveBulkCasesElasticSearch(
                accessToken,
                caseTypeId,
                multipleRef);

        return submitBulkEvents.stream()
            .map(x -> x.getCaseData().getEthosCaseReference())
            .collect(Collectors.toList());

    }

    private String generateMarkUp(String ccdGatewayBaseUrl, String caseId, String ethosCaseRef) {

        String url = ccdGatewayBaseUrl + "/cases/case-details/" + caseId;

        return "<a target=\"_blank\" href=\"" + url + "\">" + ethosCaseRef + "</a>";

    }

}
