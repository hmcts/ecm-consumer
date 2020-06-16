package uk.gov.hmcts.reform.ethos.ecm.consumer.helpers;

import uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus.CreateUpdatesMsg;
import uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus.UpdateData;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;

public class CreateUpdatesHelper {

    private static final int CHUNK_MESSAGE_SIZE = 2;

    public static List<CreateUpdatesMsg> getCreateUpdatesMessagesCollection(List<String> ethosCaseRefCollection) {

        return Partition.ofSize(ethosCaseRefCollection, CHUNK_MESSAGE_SIZE).stream()
            .map(CreateUpdatesHelper::createUpdatesMsg)
            .collect(Collectors.toList());
    }

    private static CreateUpdatesMsg createUpdatesMsg(List<String> ethosCaseRefCollection) {
        UpdateData updateData = UpdateData.builder()
            .lead("4150002/2020")
            .claimantRep("ClaimantRep")
            .build();
        return CreateUpdatesMsg.builder()
            .msgId(UUID.randomUUID().toString())
            .jurisdiction("EMPLOYMENT")
            .caseTypeId(SCOTLAND_BULK_CASE_TYPE_ID)
            .multipleRef("4150001")
            .ethosCaseRefCollection(ethosCaseRefCollection)
            .totalCases(String.valueOf(ethosCaseRefCollection.size()))
            .username("eric1.ccdcooper@gmail.com")
            .updateData(updateData)
            .build();
    }

}
