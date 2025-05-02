package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.bulk.items.CaseIdTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.multiples.MultipleData;
import uk.gov.hmcts.ecm.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CreationSingleDataModel;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.MultipleErrors;

import java.io.IOException;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ERRORED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MIGRATION_CASE_SOURCE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TRANSFERRED_STATE;

@Slf4j
@Service
public class MultipleUpdateService {
    @Value("${ccd_gateway_base_url}")
    private String ccdGatewayBaseUrl;
    private final transient CcdClient ccdClient;
    private final transient UserService userService;

    @Autowired
    public MultipleUpdateService(CcdClient ccdClient, UserService userService) {
        this.ccdClient = ccdClient;
        this.userService = userService;
    }

    public void sendUpdateToMultipleLogic(UpdateCaseMsg updateCaseMsg,
                                          List<MultipleErrors> multipleErrorsList) throws IOException {
        if (updateCaseMsg == null) {
            log.info("updateCaseMsg is null ");
            return;
        }

        String accessToken = userService.getAccessToken();
        List<SubmitMultipleEvent> submitMultipleEvents = retrieveMultipleCase(accessToken, updateCaseMsg);
        if (submitMultipleEvents != null && !submitMultipleEvents.isEmpty()) {
            if (updateCaseMsg.getDataModelParent() instanceof CreationSingleDataModel) {
                log.info("Create new multiple");
                // retrieve list of child case ids from the old multiple
                SubmitMultipleEvent newMultiple = sendMultipleCreation(
                    accessToken, updateCaseMsg, multipleErrorsList,
                    submitMultipleEvents.get(0).getCaseData().getCaseIdCollection());
                if (newMultiple != null) {
                    updateCaseMsg.setMultipleReferenceLinkMarkUp(
                        generateMarkUp(ccdGatewayBaseUrl,
                                       String.valueOf(newMultiple.getCaseId()),
                                       newMultiple.getCaseData().getMultipleReference()
                        ));
                }

                if (multipleErrorsList == null || multipleErrorsList.isEmpty()) {
                    log.info("Send update to multiple updating to transferred");
                    sendUpdate(submitMultipleEvents.get(0), accessToken, updateCaseMsg,
                        multipleErrorsList, TRANSFERRED_STATE);
                }
            } else {
                sendUpdate(submitMultipleEvents.get(0), accessToken, updateCaseMsg, multipleErrorsList, OPEN_STATE);
            }

        } else {
            log.info("No submit events found");

        }
    }

    private static String generateMarkUp(String ccdGatewayBaseUrl, String caseId, String multipleCaseRef) {
        String url = ccdGatewayBaseUrl + "/cases/case-details/" + caseId;
        return "<a target=\"_blank\" href=\"" + url + "\">" + multipleCaseRef + "</a>";
    }

    private List<SubmitMultipleEvent> retrieveMultipleCase(String authToken,
                                                           UpdateCaseMsg updateCaseMsg) throws IOException {

        return ccdClient.retrieveMultipleCasesElasticSearchWithRetries(authToken,
                                                        updateCaseMsg.getCaseTypeId(),
                                                        updateCaseMsg.getMultipleRef());
    }

    private void sendUpdate(SubmitMultipleEvent submitMultipleEvent, String accessToken, UpdateCaseMsg updateCaseMsg,
                            List<MultipleErrors> multipleErrorsList, String multipleState)
        throws IOException {
        String caseTypeId = updateCaseMsg.getCaseTypeId();
        String jurisdiction = updateCaseMsg.getJurisdiction();
        String caseId = String.valueOf(submitMultipleEvent.getCaseId());

        // Using start for event token only and no data
        CCDRequest returnedRequest = ccdClient.startBulkAmendEventForCase(accessToken,
                                                                          caseTypeId,
                                                                          jurisdiction,
                                                                          caseId);
        var multipleData = new MultipleData();

        if (multipleErrorsList != null && !multipleErrorsList.isEmpty()) {
            multipleData.setState(ERRORED_STATE);
            log.info("Updating the multiple STATE: " + ERRORED_STATE);

        } else {
            if (multipleState.equals(TRANSFERRED_STATE)) {
                String officeCT = (((CreationSingleDataModel) updateCaseMsg.getDataModelParent()).getOfficeCT());
                String reasonForCT = (((CreationSingleDataModel) updateCaseMsg.getDataModelParent()).getReasonForCT());
                String officeMultipleTransferredTo = "Transferred to " + officeCT;
                String sourceMultipleLink = updateCaseMsg.getMultipleReferenceLinkMarkUp() != null
                    ? officeMultipleTransferredTo + " " + updateCaseMsg.getMultipleReferenceLinkMarkUp()
                    : officeMultipleTransferredTo;
                multipleData.setLinkedMultipleCT(sourceMultipleLink);
                multipleData.setReasonForCT(reasonForCT);
            }

            multipleData.setState(multipleState);
            log.info("Updating the multiple STATE: " + multipleState);
        }

        ccdClient.submitMultipleEventForCase(accessToken,
                                             multipleData,
                                             caseTypeId,
                                             jurisdiction,
                                             returnedRequest,
                                             caseId);
    }

    private SubmitMultipleEvent sendMultipleCreation(String accessToken, UpdateCaseMsg updateCaseMsg,
                                      List<MultipleErrors> multipleErrorsList, List<CaseIdTypeItem> caseIds)
        throws IOException {
        if (multipleErrorsList == null || multipleErrorsList.isEmpty()) {
            String caseTypeId = (((CreationSingleDataModel) updateCaseMsg.getDataModelParent()).getOfficeCT());
            String jurisdiction = updateCaseMsg.getJurisdiction();
            MultipleData multipleData = new MultipleData();

            //Used to pull the information for the old multiple on the new multiple creation
            multipleData.setLinkedMultipleCT(updateCaseMsg.getCaseTypeId());
            multipleData.setCaseIdCollection(caseIds);
            multipleData.setMultipleSource(MIGRATION_CASE_SOURCE);
            multipleData.setMultipleReference(updateCaseMsg.getMultipleRef());

            String multipleCaseTypeId = UtilHelper.getBulkCaseTypeId(caseTypeId);
            CCDRequest returnedRequest = ccdClient.startCaseMultipleCreation(accessToken,
                                                                             multipleCaseTypeId,
                                                                             jurisdiction);
            return ccdClient.submitMultipleCreation(accessToken,
                                             multipleData,
                                             multipleCaseTypeId,
                                             jurisdiction,
                                             returnedRequest);
        }
        return null;
    }
}
