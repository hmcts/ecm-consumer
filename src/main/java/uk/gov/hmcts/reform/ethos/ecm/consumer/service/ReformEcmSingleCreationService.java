package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import java.io.IOException;
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

@Slf4j
@RequiredArgsConstructor
@Service
public class ReformEcmSingleCreationService {
    private EcmCaseDataToEt1Mapper caseDataEcmToEt1Mapper;
    private final CcdClient ccdClient;

    public void sendCreation(SubmitEvent oldSubmitEvent, String accessToken, UpdateCaseMsg updateCaseMsg)
        throws IOException {

        var transferToReformECMDataModel =
            ((TransferToReformECMDataModel) updateCaseMsg.getDataModelParent());
        String caseTypeId = transferToReformECMDataModel.getOfficeCT();
        String positionTypeCT = transferToReformECMDataModel.getPositionTypeCT();
        String ccdGatewayBaseUrl = transferToReformECMDataModel.getCcdGatewayBaseUrl();
        String jurisdiction = updateCaseMsg.getJurisdiction();
        String caseId = String.valueOf(oldSubmitEvent.getCaseId());
        String caseSourceOffice = updateCaseMsg.getCaseTypeId();
        var reasonForCT = transferToReformECMDataModel.getReasonForCT();

        log.info("Transferring new case to Reform Ecm");
        transferCase(oldSubmitEvent, caseId, caseTypeId, ccdGatewayBaseUrl, positionTypeCT, reasonForCT,
                        jurisdiction, accessToken, caseSourceOffice);
    }

    // get the source single details from creation event msg

    // retrieve the full details of the source single, i.e. its case data ccd call

    //submit case creation to et ccd baseurl
    private void transferCase(SubmitEvent oldSubmitEvent, String caseId, String caseTypeId, String ccdGatewayBaseUrl,
                                 String positionTypeCT, String reasonForCT, String jurisdiction, String accessToken, String sourceOffice)
        throws IOException {
        var newCaseDetailsCT = createCaseDetailsCaseTransfer(oldSubmitEvent.getCaseData(), caseId, caseTypeId,
                                          ccdGatewayBaseUrl, positionTypeCT, reasonForCT, jurisdiction, oldSubmitEvent.getState(),
                                                             sourceOffice);

        CCDRequest returnedRequest = ccdClient.startCaseCreationTransfer(accessToken, newCaseDetailsCT);

        ccdClient.submitCaseCreation(accessToken, newCaseDetailsCT, returnedRequest);

    }

    private uk.gov.hmcts.et.common.model.ccd.CaseDetails createCaseDetailsCaseTransfer(CaseData oldCaseData,
                                                                                       String caseId, String caseTypeId,
                                                      String ccdGatewayBaseUrl, String positionTypeCT, String reasonForCT,
                                                      String jurisdiction, String caseState, String sourceOffice) {

        var newCaseTransferCaseDetails = new uk.gov.hmcts.et.common.model.ccd.CaseDetails();
        newCaseTransferCaseDetails.setCaseTypeId(caseTypeId);
        newCaseTransferCaseDetails.setJurisdiction(jurisdiction);
        //map the case data details to Et1CaseData details
        var et1CaseData = copyCaseDataToEt1CaseData(oldCaseData, caseId, ccdGatewayBaseUrl, positionTypeCT,
                                                    reasonForCT, caseState, sourceOffice);
        newCaseTransferCaseDetails.setCaseData(et1CaseData);

        return newCaseTransferCaseDetails;
    }

    private uk.gov.hmcts.et.common.model.ccd.CaseData copyCaseDataToEt1CaseData(CaseData ecmCaseData, String caseId,
                                                  String ccdGatewayBaseUrl, String positionTypeCT,
                                                  String reasonForCT, String caseState, String sourceOffice) {
        return EcmCaseDataToEt1Mapper.getEt1CaseData(ecmCaseData, caseId, ccdGatewayBaseUrl, positionTypeCT,
                                                     reasonForCT, caseState, sourceOffice);
    }
}
