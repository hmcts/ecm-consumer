package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.TransferToReformECMDataModel;
import java.io.IOException;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;

@Slf4j
@RequiredArgsConstructor
@Service
public class SingleTransferToReformEcmService {
    private final CcdClient ccdClient;

    public void sendEcmCaseTransferred(SubmitEvent submitEvent, String accessToken,
                                       UpdateCaseMsg updateCaseMsg) throws IOException {
        var transferToReformECMDataModel =
            ((TransferToReformECMDataModel) updateCaseMsg.getDataModelParent());
        var positionType = transferToReformECMDataModel.getPositionType();
        var caseTypeIdCT = transferToReformECMDataModel.getOfficeCT();
        var reasonForCT = transferToReformECMDataModel.getReasonForCT();
        var jurisdiction = updateCaseMsg.getJurisdiction();

        String caseTypeId = !updateCaseMsg.getMultipleRef().equals(SINGLE_CASE_TYPE)
            ? UtilHelper.getCaseTypeId(updateCaseMsg.getCaseTypeId())
            : updateCaseMsg.getCaseTypeId();

        updateEcmCaseTransferredToReformEcm(submitEvent, caseTypeId, caseTypeIdCT, jurisdiction, accessToken,
                                            positionType, reasonForCT);
    }

    private void updateEcmCaseTransferredToReformEcm(SubmitEvent submitEvent, String caseTypeId, String caseTypeIdCT,
                                                     String jurisdiction, String accessToken, String positionType,
                                                     String reasonForCT) throws IOException {
        submitEvent.getCaseData().setLinkedCaseCT("Transferred to Reform ECM : " + caseTypeIdCT);
        log.info("Setting positionType to Reform ECM positionType: " + positionType
                     + " for ECM case: " + submitEvent.getCaseData().getEthosCaseReference());
        submitEvent.getCaseData().setPositionType(positionType);
        submitEvent.getCaseData().setPositionTypeCT("Case transferred to Reform ECM");
        submitEvent.getCaseData().setReasonForCT(reasonForCT);

        CCDRequest returnedRequest = ccdClient.startCaseTransfer(accessToken, caseTypeId, jurisdiction,
                                                                 String.valueOf(submitEvent.getCaseId()));
        ccdClient.submitEventForCase(accessToken,
                                     submitEvent.getCaseData(),
                                     caseTypeId,
                                     jurisdiction,
                                     returnedRequest,
                                     String.valueOf(submitEvent.getCaseId()));
    }
}
