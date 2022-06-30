package uk.gov.hmcts.reform.ethos.ecm.consumer.helpers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ecm.common.model.ccd.Address;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertEquals(expectedCaseData.getEthosCaseReference(), caseData.getEthosCaseReference());
        assertEquals(REASON_FOR_TRANSFER, expectedCaseData.getReasonForCT());
    }

}
