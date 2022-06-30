package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.TransferToReformECMDataModel;
import uk.gov.hmcts.reform.ethos.ecm.consumer.helpers.EcmCaseDataToEt1Mapper;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReformEcmSingleCreationService {
    private final CcdClient ccdClient;

    public void sendCreation(SubmitEvent oldSubmitEvent, String accessToken, UpdateCaseMsg updateCaseMsg)
        throws IOException {

        var transferToReformECMDataModel = ((TransferToReformECMDataModel)
            updateCaseMsg.getDataModelParent());
        String ccdGatewayBaseUrl = transferToReformECMDataModel.getCcdGatewayBaseUrl();
        String jurisdiction = updateCaseMsg.getJurisdiction();
        String caseId = String.valueOf(oldSubmitEvent.getCaseId());
        String caseSourceOffice = updateCaseMsg.getCaseTypeId();

        log.info("Transferring new case to Reform Ecm");
        transferCase(oldSubmitEvent, transferToReformECMDataModel, caseId, ccdGatewayBaseUrl, jurisdiction,
                     accessToken, caseSourceOffice
        );
    }

    private void transferCase(SubmitEvent oldSubmitEvent, TransferToReformECMDataModel dataModel, String caseId,
                              String ccdGatewayBaseUrl, String jurisdiction, String accessToken,
                              String sourceOffice) throws IOException {
        var newCaseDetailsCT = createCaseDetailsCaseTransfer(oldSubmitEvent.getCaseData(),
                                                             caseId,
                                                             dataModel,
                                                             ccdGatewayBaseUrl,
                                                             jurisdiction,
                                                             sourceOffice
        );

        CCDRequest returnedRequest = ccdClient.startCaseCreationTransfer(accessToken, newCaseDetailsCT);
        ccdClient.submitCaseCreation(accessToken, newCaseDetailsCT, returnedRequest);
    }

    private uk.gov.hmcts.et.common.model.ccd.CaseDetails createCaseDetailsCaseTransfer(CaseData oldCaseData,
                                                                                       String caseId,
                                                                                       TransferToReformECMDataModel
                                                                                           dataModel,
                                                                                       String ccdGatewayBaseUrl,
                                                                                       String jurisdiction,
                                                                                       String sourceOffice) {
        var caseTypeId = dataModel.getOfficeCT();
        var positionType = dataModel.getPositionType();
        var reasonForCT = dataModel.getReasonForCT();
        var newCaseTransferCaseDetails = new uk.gov.hmcts.et.common.model.ccd.CaseDetails();
        newCaseTransferCaseDetails.setCaseTypeId(caseTypeId);
        newCaseTransferCaseDetails.setJurisdiction(jurisdiction);
        var et1CaseData = copyCaseDataToEt1CaseData(oldCaseData, caseId, ccdGatewayBaseUrl, positionType,
                                                    reasonForCT, sourceOffice
        );
        newCaseTransferCaseDetails.setCaseData(et1CaseData);

        return newCaseTransferCaseDetails;
    }

    private uk.gov.hmcts.et.common.model.ccd.CaseData copyCaseDataToEt1CaseData(CaseData ecmCaseData, String caseId,
                                                                                String ccdGatewayBaseUrl,
                                                                                String positionType, String reasonForCT,
                                                                                String sourceOffice) {
        return EcmCaseDataToEt1Mapper.getEt1CaseData(ecmCaseData, caseId, ccdGatewayBaseUrl, positionType,
                                                     reasonForCT, sourceOffice
        );
    }
}
