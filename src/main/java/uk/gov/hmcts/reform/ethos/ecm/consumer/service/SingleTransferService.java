package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CreationSingleDataModel;

import java.io.IOException;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE;

@Slf4j
@RequiredArgsConstructor
@Service
public class SingleTransferService {

    private final CcdClient ccdClient;

    public void sendTransferred(SubmitEvent submitEvent, String accessToken,
                             UpdateCaseMsg updateCaseMsg) throws IOException {

        CreationSingleDataModel creationSingleDataModel =
            ((CreationSingleDataModel) updateCaseMsg.getDataModelParent());
        String positionTypeCT = creationSingleDataModel.getPositionTypeCT();
        String caseTypeIdCT = creationSingleDataModel.getOfficeCT();
        String reasonForCT = creationSingleDataModel.getReasonForCT();

        String jurisdiction = updateCaseMsg.getJurisdiction();
        String caseTypeId = UtilHelper.getCaseTypeId(updateCaseMsg.getCaseTypeId());

        updateTransferredCase(submitEvent, caseTypeId, caseTypeIdCT, jurisdiction, accessToken, positionTypeCT,
                              reasonForCT);

    }

    private void updateTransferredCase(SubmitEvent submitEvent, String caseTypeId, String caseTypeIdCT,
                                       String jurisdiction, String accessToken, String positionTypeCT,
                                       String reasonForCT) throws IOException {

        CCDRequest returnedRequest = ccdClient.startCaseTransfer(accessToken, caseTypeId, jurisdiction,
                                                                 String.valueOf(submitEvent.getCaseId()));

        generateCaseData(submitEvent.getCaseData(), caseTypeIdCT, positionTypeCT, reasonForCT);

        ccdClient.submitEventForCase(accessToken,
                                     submitEvent.getCaseData(),
                                     caseTypeId,
                                     jurisdiction,
                                     returnedRequest,
                                     String.valueOf(submitEvent.getCaseId()));

    }

    private void generateCaseData(CaseData caseData, String caseTypeIdCT, String positionTypeCT, String reasonForCT) {

        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode(caseTypeIdCT);
        dynamicFixedListType.setValue(dynamicValueType);

        caseData.setOfficeCT(dynamicFixedListType);
        caseData.setStateAPI(MULTIPLE);
        caseData.setPositionTypeCT(positionTypeCT);
        caseData.setReasonForCT(reasonForCT);

    }

}
