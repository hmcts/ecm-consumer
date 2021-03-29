package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CreationSingleDataModel;

import java.io.IOException;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
@RequiredArgsConstructor
@Service
public class SingleCreationService {

    private final CcdClient ccdClient;

    public void sendCreation(SubmitEvent submitEvent, String accessToken,
                             UpdateCaseMsg updateCaseMsg) throws IOException {

        CreationSingleDataModel creationSingleDataModel =
            ((CreationSingleDataModel) updateCaseMsg.getDataModelParent());
        String caseTypeId = creationSingleDataModel.getOfficeCT();
        String positionTypeCT = creationSingleDataModel.getPositionTypeCT();
        String ccdGatewayBaseUrl = creationSingleDataModel.getCcdGatewayBaseUrl();
        String jurisdiction = updateCaseMsg.getJurisdiction();
        String caseId = String.valueOf(submitEvent.getCaseId());

        CaseDetails newCaseDetailsCT = createCaseDetailsCaseTransfer(submitEvent.getCaseData(), caseId, caseTypeId,
                                                                     ccdGatewayBaseUrl, positionTypeCT, jurisdiction);

        CCDRequest returnedRequest = getStartCaseCreationByState(accessToken,
                                                                 submitEvent.getCaseData(),
                                                                 newCaseDetailsCT);

        ccdClient.submitCaseCreation(accessToken,
                                     newCaseDetailsCT,
                                     returnedRequest);

    }

    private CaseDetails createCaseDetailsCaseTransfer(CaseData caseData, String caseId, String caseTypeId,
                                                      String ccdGatewayBaseUrl, String positionTypeCT,
                                                      String jurisdiction) {

        CaseDetails newCaseTransferCaseDetails = new CaseDetails();
        newCaseTransferCaseDetails.setCaseTypeId(caseTypeId);
        newCaseTransferCaseDetails.setJurisdiction(jurisdiction);
        newCaseTransferCaseDetails.setCaseData(
            generateCaseDataCaseTransfer(caseData, caseId, ccdGatewayBaseUrl, positionTypeCT));
        return newCaseTransferCaseDetails;

    }

    private CaseData generateCaseDataCaseTransfer(CaseData caseData, String caseId,
                                                  String ccdGatewayBaseUrl, String positionTypeCT) {

        CaseData newCaseData = new CaseData();
        newCaseData.setEthosCaseReference(caseData.getEthosCaseReference());
        newCaseData.setPositionType(positionTypeCT);
        newCaseData.setCaseType(caseData.getCaseType());
        newCaseData.setClaimantTypeOfClaimant(caseData.getClaimantTypeOfClaimant());
        newCaseData.setClaimantCompany(caseData.getClaimantCompany());
        newCaseData.setClaimantIndType(caseData.getClaimantIndType());
        newCaseData.setClaimantType(caseData.getClaimantType());
        newCaseData.setClaimantOtherType(caseData.getClaimantOtherType());
        newCaseData.setPreAcceptCase(caseData.getPreAcceptCase());
        newCaseData.setReceiptDate(caseData.getReceiptDate());
        newCaseData.setFeeGroupReference(caseData.getFeeGroupReference());
        newCaseData.setClaimantWorkAddressQuestion(caseData.getClaimantWorkAddressQuestion());
        newCaseData.setClaimantWorkAddressQRespondent(caseData.getClaimantWorkAddressQRespondent());
        newCaseData.setRepresentativeClaimantType(caseData.getRepresentativeClaimantType());
        newCaseData.setRespondentCollection(caseData.getRespondentCollection());
        newCaseData.setRepCollection(caseData.getRepCollection());
        newCaseData.setPositionType(caseData.getPositionTypeCT());
        newCaseData.setDateToPosition(caseData.getDateToPosition());
        newCaseData.setCurrentPosition(caseData.getCurrentPosition());
        newCaseData.setDepositCollection(caseData.getDepositCollection());
        newCaseData.setJudgementCollection(caseData.getJudgementCollection());
        newCaseData.setJurCodesCollection(caseData.getJurCodesCollection());
        newCaseData.setBfActions(caseData.getBfActions());
        newCaseData.setUserLocation(caseData.getUserLocation());
        newCaseData.setDocumentCollection(caseData.getDocumentCollection());
        newCaseData.setAdditionalCaseInfoType(caseData.getAdditionalCaseInfoType());
        newCaseData.setCaseNotes(caseData.getCaseNotes());
        newCaseData.setClaimantWorkAddress(caseData.getClaimantWorkAddress());
        newCaseData.setClaimantRepresentedQuestion(caseData.getClaimantRepresentedQuestion());
        newCaseData.setCaseSource(caseData.getCaseSource());
        newCaseData.setEt3Received(caseData.getEt3Received());
        newCaseData.setConciliationTrack(caseData.getConciliationTrack());
        newCaseData.setCounterClaim(caseData.getCounterClaim());
        newCaseData.setRestrictedReporting(caseData.getRestrictedReporting());
        newCaseData.setRespondent(caseData.getRespondent());
        newCaseData.setClaimant(caseData.getClaimant());
        newCaseData.setCaseRefECC(caseData.getCaseRefECC());
        newCaseData.setCcdID(caseData.getCcdID());
        newCaseData.setFlagsImageAltText(caseData.getFlagsImageAltText());
        newCaseData.setCompanyPremises(caseData.getCompanyPremises());

        newCaseData.setLinkedCaseCT(generateMarkUp(ccdGatewayBaseUrl, caseId, caseData.getEthosCaseReference()));

        return newCaseData;

    }

    private CCDRequest getStartCaseCreationByState(String accessToken, CaseData caseData,
                                                   CaseDetails newCaseTransferCaseDetails) throws IOException {

        if (caseData.getPreAcceptCase() != null
            && caseData.getPreAcceptCase().getCaseAccepted() != null
            && caseData.getPreAcceptCase().getCaseAccepted().equals(YES)) {

            return ccdClient.startCaseCreationAccepted(accessToken, newCaseTransferCaseDetails);

        } else {

            return ccdClient.startCaseCreationSubmitted(accessToken, newCaseTransferCaseDetails);

        }
    }

    private String generateMarkUp(String ccdGatewayBaseUrl, String caseId, String ethosCaseRef) {

        String url = ccdGatewayBaseUrl + "/cases/case-details/" + caseId;

        return "<a target=\"_blank\" href=\"" + url + "\">" + ethosCaseRef + "</a>";

    }

}
