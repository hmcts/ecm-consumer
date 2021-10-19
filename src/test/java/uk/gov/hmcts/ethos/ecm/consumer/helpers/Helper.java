package uk.gov.hmcts.ethos.ecm.consumer.helpers;

import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CloseDataModel;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CreationDataModel;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CreationSingleDataModel;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.PreAcceptDataModel;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

public class Helper {

    private static final String CASE_NUMBER1 = "4150002/2020";
    private static final String MULTIPLE_CASE = "4150001";
    private static final String JURISDICTION = "EMPLOYMENT";
    private static final String USERNAME = "eric.ccdcooper@gmail.com";

    private Helper() {
    }

    private static final CreationDataModel creationDataModel = CreationDataModel.builder()
        .lead(CASE_NUMBER1)
        .multipleRef(MULTIPLE_CASE)
        .build();

    private static final PreAcceptDataModel preAcceptDataModel = PreAcceptDataModel.builder().build();

    private static final CloseDataModel closeDataModel = CloseDataModel.builder().build();

    private static final CreationSingleDataModel creationSingleDataModel = CreationSingleDataModel.builder()
        .positionTypeCT("PositionTypeCT")
        .officeCT("Manchester")
        .ccdGatewayBaseUrl("ccdGatewayBaseUrl")
        .build();

    public static UpdateCaseMsg generateUpdateCaseMsg() {
        return UpdateCaseMsg.builder()
            .msgId("1")
            .jurisdiction(JURISDICTION)
            .caseTypeId(SCOTLAND_BULK_CASE_TYPE_ID)
            .multipleRef(MULTIPLE_CASE)
            .ethosCaseReference(CASE_NUMBER1)
            .totalCases("1")
            .username(USERNAME)
            .confirmation(YES)
            .dataModelParent(creationDataModel)
            .build();
    }

    public static UpdateCaseMsg generatePreAcceptCaseMsg() {
        return UpdateCaseMsg.builder()
            .msgId("1")
            .jurisdiction(JURISDICTION)
            .caseTypeId(SCOTLAND_BULK_CASE_TYPE_ID)
            .multipleRef(MULTIPLE_CASE)
            .ethosCaseReference(CASE_NUMBER1)
            .totalCases("1")
            .username(USERNAME)
            .confirmation(YES)
            .dataModelParent(preAcceptDataModel)
            .build();
    }

    public static UpdateCaseMsg generateCloseCaseMsg() {
        return UpdateCaseMsg.builder()
            .msgId("1")
            .jurisdiction(JURISDICTION)
            .caseTypeId(SCOTLAND_BULK_CASE_TYPE_ID)
            .multipleRef(MULTIPLE_CASE)
            .ethosCaseReference(CASE_NUMBER1)
            .totalCases("1")
            .username(USERNAME)
            .confirmation(NO)
            .dataModelParent(closeDataModel)
            .build();
    }

    public static CreateUpdatesMsg generateCreateUpdatesMsg() {
        return CreateUpdatesMsg.builder()
            .msgId("1")
            .jurisdiction(JURISDICTION)
            .caseTypeId(SCOTLAND_BULK_CASE_TYPE_ID)
            .multipleRef(MULTIPLE_CASE)
            .ethosCaseRefCollection(Arrays.asList("4150001/2020", CASE_NUMBER1, "4150003/2020"))
            .totalCases("3")
            .username(USERNAME)
            .confirmation(YES)
            .dataModelParent(creationDataModel)
            .build();
    }

    public static UpdateCaseMsg generateCreationSingleCaseMsg() {
        return UpdateCaseMsg.builder()
            .msgId("1")
            .jurisdiction(JURISDICTION)
            .caseTypeId(SCOTLAND_BULK_CASE_TYPE_ID)
            .multipleRef(MULTIPLE_CASE)
            .ethosCaseReference(CASE_NUMBER1)
            .totalCases("1")
            .username(USERNAME)
            .confirmation(NO)
            .dataModelParent(creationSingleDataModel)
            .build();
    }

    public static CompletableFuture<Void> getCompletableFuture() {
        return CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        });
    }
}
