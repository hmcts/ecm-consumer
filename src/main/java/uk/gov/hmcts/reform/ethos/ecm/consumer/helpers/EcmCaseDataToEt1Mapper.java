package uk.gov.hmcts.reform.ethos.ecm.consumer.helpers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CasePreAcceptType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantOtherType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantWorkAddressType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EcmCaseDataToEt1Mapper {

    private EcmCaseDataToEt1Mapper() {
        // All access through static methods
    }

    public static CaseData getEt1CaseData(uk.gov.hmcts.ecm.common.model.ccd.CaseData oldCaseData, String caseId,
                                          String ccdGatewayBaseUrl, String positionType, String reasonForCT,
                                          String sourceOffice) {
        Address etTribunalAddress = new Address();
        var ecmTribunalAddress = oldCaseData.getTribunalCorrespondenceAddress();
        etTribunalAddress.setAddressLine1(ecmTribunalAddress.getAddressLine1());
        etTribunalAddress.setAddressLine2(ecmTribunalAddress.getAddressLine2());
        etTribunalAddress.setCounty(ecmTribunalAddress.getCounty());
        etTribunalAddress.setCountry(ecmTribunalAddress.getCountry());
        etTribunalAddress.setPostCode(ecmTribunalAddress.getPostCode());

        var caseData = new uk.gov.hmcts.et.common.model.ccd.CaseData();
        caseData.setTribunalCorrespondenceAddress(etTribunalAddress);
        caseData.setTribunalCorrespondenceTelephone(oldCaseData.getTribunalCorrespondenceTelephone());
        caseData.setTribunalCorrespondenceDX(oldCaseData.getTribunalCorrespondenceDX());
        caseData.setReceiptDate(oldCaseData.getReceiptDate());
        caseData.setFeeGroupReference(oldCaseData.getFeeGroupReference());
        caseData.setPositionType(positionType);
        caseData.setEcmCaseType(oldCaseData.getEcmCaseType());

        setManagingOffice(oldCaseData, caseData, sourceOffice);

        if (oldCaseData.getPreAcceptCase() != null) {
            var preAccept = new CasePreAcceptType();
            preAccept.setDateAccepted(oldCaseData.getPreAcceptCase().getDateAccepted());
            preAccept.setCaseAccepted(oldCaseData.getPreAcceptCase().getCaseAccepted());
            caseData.setPreAcceptCase(preAccept);
        }

        caseData.setEcmCaseType(oldCaseData.getEcmCaseType());
        caseData.setCaseSource(oldCaseData.getCaseSource());
        caseData.setClaimantRepresentedQuestion(oldCaseData.getClaimantRepresentedQuestion());
        caseData.setClaimantIndType((ClaimantIndType) objectMapper(oldCaseData.getClaimantIndType(),
                                                                   ClaimantIndType.class));
        caseData.setClaimantType((ClaimantType) objectMapper(oldCaseData.getClaimantType(), ClaimantType.class));
        caseData.setRepresentativeClaimantType((RepresentedTypeC)
                                                   objectMapper(oldCaseData.getRepresentativeClaimantType(),
                                                                RepresentedTypeC.class));
        caseData.setClaimantOtherType((ClaimantOtherType) objectMapper(oldCaseData.getClaimantOtherType(),
                                                                       ClaimantOtherType.class));
        caseData.setRespondentCollection(createRespondentCollection(oldCaseData.getRespondentCollection()));
        caseData.setRespondent(oldCaseData.getRespondent());
        caseData.setClaimant(oldCaseData.getClaimant());
        caseData.setClaimantTypeOfClaimant(oldCaseData.getClaimantTypeOfClaimant());
        caseData.setClaimantCompany(oldCaseData.getClaimantCompany());

        caseData.setClaimantWorkAddressQuestion(oldCaseData.getClaimantWorkAddressQuestion());
        caseData.setClaimantWorkAddressQRespondent(
            (DynamicFixedListType) objectMapper(oldCaseData.getClaimantWorkAddressQRespondent(),
                                                DynamicFixedListType.class));
        caseData.setClaimantWorkAddress((ClaimantWorkAddressType) objectMapper(oldCaseData.getClaimantWorkAddress(),
                                                                               ClaimantWorkAddressType.class));
        caseData.setLinkedCaseCT(generateMarkUp(ccdGatewayBaseUrl, caseId, oldCaseData.getEthosCaseReference()));
        caseData.setReasonForCT(reasonForCT);
        caseData.setDocumentCollection(createDocumentCollection(oldCaseData.getDocumentCollection()));

        return caseData;
    }

    private static void setManagingOffice(uk.gov.hmcts.ecm.common.model.ccd.CaseData oldCaseData,
                                   uk.gov.hmcts.et.common.model.ccd.CaseData caseData,
                                          String sourceOffice) {
        if (StringUtils.hasLength(oldCaseData.getManagingOffice())) {
            caseData.setManagingOffice("Glasgow");
        } else {
            caseData.setManagingOffice(sourceOffice);
        }
    }

    private static List<RespondentSumTypeItem> createRespondentCollection(
        List<uk.gov.hmcts.ecm.common.model.ccd.items.RespondentSumTypeItem> respondentCollection) {

        List<RespondentSumTypeItem> respondentSumTypeItems = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(respondentCollection)) {
            for (var respondent : respondentCollection) {
                var respondentSumType = (RespondentSumType) objectMapper(respondent.getValue(),
                                                                         RespondentSumType.class);
                var respondentSumTypeItem = new RespondentSumTypeItem();
                respondentSumTypeItem.setId(UUID.randomUUID().toString());
                respondentSumTypeItem.setValue(respondentSumType);
                respondentSumTypeItems.add(respondentSumTypeItem);
            }
        }

        return respondentSumTypeItems;
    }

    private static List<DocumentTypeItem> createDocumentCollection(
        List<uk.gov.hmcts.ecm.common.model.ccd.items.DocumentTypeItem> documentCollection) {

        List<uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem> documentTypeItemsList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(documentCollection)) {
            for (var document : documentCollection) {
                var documentType = new uk.gov.hmcts.et.common.model.ccd.types.DocumentType();
                documentType.setOwnerDocument(document.getValue().getOwnerDocument());
                documentType.setTypeOfDocument(document.getValue().getTypeOfDocument());
                documentType.setUploadedDocument((uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType)
                                                     objectMapper(
                    document.getValue().getUploadedDocument(),
                    uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType.class));
                documentType.setCreationDate(document.getValue().getCreationDate());
                documentType.setShortDescription(document.getValue().getShortDescription());

                var documentTypeItem = new DocumentTypeItem();
                documentTypeItem.setId(UUID.randomUUID().toString());
                documentTypeItem.setValue(documentType);
                documentTypeItemsList.add(documentTypeItem);
            }
        }
        return documentTypeItemsList;
    }

    private static String generateMarkUp(String ccdGatewayBaseUrl, String caseId, String ethosCaseRef) {
        var url = ccdGatewayBaseUrl + "/cases/case-details/" + caseId;
        return "<a target=\"_blank\" href=\"" + url + "\">" + ethosCaseRef + "</a>";
    }

    public static Object objectMapper(Object object, Class<?> classType) {
        var mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper.convertValue(object, classType);
    }

}
