package uk.gov.hmcts.reform.ethos.ecm.consumer.tasks;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.bulk.BulkData;
import uk.gov.hmcts.ecm.common.model.bulk.SubmitBulkEvent;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;

import java.io.IOException;

@Slf4j
public class MultipleStateTask implements Runnable {

    private SubmitBulkEvent submitBulkEvent;
    private String authToken;
    private String jurisdiction;
    private String caseTypeId;
    private CcdClient ccdClient;

    public MultipleStateTask(String jurisdiction, String caseTypeId, SubmitBulkEvent submitBulkEvent, String authToken, CcdClient ccdClient) {
        this.jurisdiction = jurisdiction;
        this.caseTypeId = caseTypeId;
        this.submitBulkEvent = submitBulkEvent;
        this.authToken = authToken;
        this.ccdClient = ccdClient;
    }

    @Override
    public void run() {

        log.info("Waiting: " + Thread.currentThread().getName());
        //String caseId = String.valueOf(submitBulkEvent.getCaseId());
        String caseId = "1589444647147690";
        try {
            CCDRequest returnedRequest = ccdClient.startBulkEventForCase(authToken, caseTypeId, jurisdiction, caseId);
            log.info("Renaming Multiple Name");
            //submitBulkEvent.getCaseData().setState(CLOSED_STATE);
            SubmitBulkEvent submitBulkEvent1 = new SubmitBulkEvent();
            submitBulkEvent1.setCaseData(new BulkData());
            submitBulkEvent1.getCaseData().setBulkCaseTitle("NAME CHANGED AGAIN");
            log.info("Sending: " + submitBulkEvent1.getCaseData());
            ccdClient.submitBulkEventForCase(authToken, submitBulkEvent1.getCaseData(), caseTypeId, jurisdiction, returnedRequest, caseId);
        } catch (IOException e) {
            log.error("Error processing bulk pre accept threads");
        }
    }
}
