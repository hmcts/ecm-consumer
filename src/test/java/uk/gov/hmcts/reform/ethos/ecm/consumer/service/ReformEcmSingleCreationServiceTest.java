package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.ccd.Address;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ecm.common.model.ccd.types.CasePreAcceptType;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.reform.ethos.ecm.consumer.helpers.Helper;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@RunWith(SpringJUnit4ClassRunner.class)
public class ReformEcmSingleCreationServiceTest {
    @InjectMocks
    private transient ReformEcmSingleCreationService reformEcmSingleCreationService;
    @Mock
    private transient CcdClient ccdClient;
    private transient SubmitEvent submitEvent;
    private transient UpdateCaseMsg updateCaseMsg;
    private transient String userToken;

    @Test
    public void sendCreation() throws IOException {
        submitEvent = new SubmitEvent();
        CaseData caseData = new CaseData();
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

        submitEvent.setCaseData(caseData);
        submitEvent.setState(ACCEPTED_STATE);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        updateCaseMsg = Helper.generateReformEcmCreationSingleCaseMsg();
        userToken = "accessToken";
        reformEcmSingleCreationService.sendCreation(submitEvent, userToken, updateCaseMsg);
        verify(ccdClient).startCaseCreationTransfer(eq(userToken),
                                                    any(uk.gov.hmcts.et.common.model.ccd.CaseDetails.class));
        verify(ccdClient).submitCaseCreation(eq(userToken),
                                             any(uk.gov.hmcts.et.common.model.ccd.CaseDetails.class),
                                             any());
        verifyNoMoreInteractions(ccdClient);
    }
}
