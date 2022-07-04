package uk.gov.hmcts.reform.ethos.ecm.consumer.helpers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ecm.common.model.ccd.Address;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType;
import uk.gov.hmcts.ecm.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.ecm.common.model.ccd.types.RespondentSumType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@RunWith(SpringJUnit4ClassRunner.class)
public class EcmCaseDataToEt1MapperTest {

    private transient String userToken;
    private transient CaseData caseData;
    static final String TRANSFERRED_POSITION_TYPE = "Case transferred to Reform ECM";
    static final String REASON_FOR_TRANSFER = "Reform Ecm Transfer test";

    @Before
    public void setUp() {
        caseData = new CaseData();
        caseData.setEthosCaseReference("1800032/2021");
        CasePreAcceptType casePreAcceptType = new CasePreAcceptType();
        casePreAcceptType.setCaseAccepted(YES);
        casePreAcceptType.setDateAccepted("2021-12-12");
        caseData.setPreAcceptCase(casePreAcceptType);
        var address = new Address();
        address.setAddressLine1("12");
        address.setAddressLine2("12");
        address.setCounty("northLeeds");
        address.setCountry("UK");
        address.setPostCode("Ld5 4LX");
        caseData.setTribunalCorrespondenceAddress(address);

        userToken = "accessToken";
    }

    @Test
    public void shouldGetEt1CaseDataFromEcmCaseData() {
        uk.gov.hmcts.et.common.model.ccd.CaseData expectedCaseData = EcmCaseDataToEt1Mapper
            .getEt1CaseData(caseData, userToken, "test_base_url", TRANSFERRED_POSITION_TYPE,
                            REASON_FOR_TRANSFER, "Leeds");

        assertEquals(expectedCaseData.getClass(), uk.gov.hmcts.et.common.model.ccd.CaseData.class);
        assertNull(expectedCaseData.getEthosCaseReference());
        assertEquals(REASON_FOR_TRANSFER, expectedCaseData.getReasonForCT());
    }

    @Test
    public void shouldGetEt1CaseDataFromEcmCaseDataWithPreAcceptSet() {
        uk.gov.hmcts.et.common.model.ccd.CaseData expectedCaseData = EcmCaseDataToEt1Mapper
            .getEt1CaseData(caseData, userToken, "test_base_url", TRANSFERRED_POSITION_TYPE,
                            REASON_FOR_TRANSFER, "Leeds");

        assertEquals(expectedCaseData.getClass(), uk.gov.hmcts.et.common.model.ccd.CaseData.class);
        assertNotNull(expectedCaseData.getPreAcceptCase());
        assertEquals(caseData.getPreAcceptCase().getDateAccepted(),
                     expectedCaseData.getPreAcceptCase().getDateAccepted());
        assertEquals(REASON_FOR_TRANSFER, expectedCaseData.getReasonForCT());
    }

    @Test
    public void shouldGetEt1CaseDataFromEcmCaseDataWithManagingOfficeSet() {
        caseData.setManagingOffice("Aberdeen");
        uk.gov.hmcts.et.common.model.ccd.CaseData expectedCaseData = EcmCaseDataToEt1Mapper
            .getEt1CaseData(caseData, userToken, "test_base_url", TRANSFERRED_POSITION_TYPE,
                            REASON_FOR_TRANSFER, "Scotland");

        assertEquals(expectedCaseData.getClass(), uk.gov.hmcts.et.common.model.ccd.CaseData.class);
        assertEquals("Glasgow", expectedCaseData.getManagingOffice());
        assertEquals(REASON_FOR_TRANSFER, expectedCaseData.getReasonForCT());
    }

    @Test
    public void shouldGetEt1CaseDataFromEcmCaseDataWithRespondentCollection() {
        var respondentSumType = new RespondentSumType();
        var respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        respondentSumTypeItem.setId(UUID.randomUUID().toString());
        respondentSumTypeItem.getValue().setRespondentName("Test Respondent");
        List<RespondentSumTypeItem> respondentSumTypeItems = new ArrayList<>();
        respondentSumTypeItems.add(respondentSumTypeItem);

        caseData.setRespondentCollection(respondentSumTypeItems);
        uk.gov.hmcts.et.common.model.ccd.CaseData expectedCaseData = EcmCaseDataToEt1Mapper
            .getEt1CaseData(caseData, userToken, "test_base_url", TRANSFERRED_POSITION_TYPE,
                            REASON_FOR_TRANSFER, "Scotland");

        assertEquals(expectedCaseData.getClass(), uk.gov.hmcts.et.common.model.ccd.CaseData.class);
        assertEquals(respondentSumTypeItem.getValue().getRespondentName(),
                     expectedCaseData.getRespondentCollection().get(0).getValue().getRespondentName());
        assertEquals(REASON_FOR_TRANSFER, expectedCaseData.getReasonForCT());

    }

    @Test
    public void shouldGetEt1CaseDataFromEcmCaseDataWithDocumentCollection() {
        List<DocumentTypeItem> docs = new ArrayList<>();
        var docTypeItem = new DocumentTypeItem();
        var docType = new DocumentType();
        docType.setShortDescription("Test doc description");
        docTypeItem.setValue(docType);
        docs.add(docTypeItem);
        caseData.setDocumentCollection(docs);

        uk.gov.hmcts.et.common.model.ccd.CaseData expectedCaseData = EcmCaseDataToEt1Mapper
            .getEt1CaseData(caseData, userToken, "test_base_url", TRANSFERRED_POSITION_TYPE,
                            REASON_FOR_TRANSFER, "Scotland");

        assertEquals(expectedCaseData.getClass(), uk.gov.hmcts.et.common.model.ccd.CaseData.class);
        assertNotNull(expectedCaseData.getDocumentCollection());
        assertEquals(docType.getShortDescription(),
                     expectedCaseData.getDocumentCollection().get(0).getValue().getShortDescription());
        assertEquals(REASON_FOR_TRANSFER, expectedCaseData.getReasonForCT());
    }
}
