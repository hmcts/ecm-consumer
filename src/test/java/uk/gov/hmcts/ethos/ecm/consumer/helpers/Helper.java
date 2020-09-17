package uk.gov.hmcts.ethos.ecm.consumer.helpers;

import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CreationDataModel;

import java.util.Arrays;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

public class Helper {

    private static CreationDataModel creationDataModel = CreationDataModel.builder()
        .lead("4150002/2020")
        .multipleRef("4150001")
        .build();

    public static UpdateCaseMsg generateUpdateCaseMsg() {
        return UpdateCaseMsg.builder()
            .msgId("1")
            .jurisdiction("EMPLOYMENT")
            .caseTypeId(SCOTLAND_BULK_CASE_TYPE_ID)
            .multipleRef("4150001")
            .ethosCaseReference("4150002/2020")
            .totalCases("1")
            .username("eric.ccdcooper@gmail.com")
            .confirmation(YES)
            .dataModelParent(creationDataModel)
            .build();
    }

    public static CreateUpdatesMsg generateCreateUpdatesMsg() {
        return CreateUpdatesMsg.builder()
            .msgId("1")
            .jurisdiction("EMPLOYMENT")
            .caseTypeId(SCOTLAND_BULK_CASE_TYPE_ID)
            .multipleRef("4150001")
            .ethosCaseRefCollection(Arrays.asList("4150001/2020", "4150002/2020", "4150003/2020"))
            .totalCases("3")
            .username("eric.ccdcooper@gmail.com")
            .confirmation(YES)
            .dataModelParent(creationDataModel)
            .build();
    }

}
