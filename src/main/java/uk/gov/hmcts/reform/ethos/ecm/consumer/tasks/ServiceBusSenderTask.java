package uk.gov.hmcts.reform.ethos.ecm.consumer.tasks;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ethos.ecm.consumer.model.UpdateCaseMsg;
import uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus.ServiceBusSender;

import static uk.gov.hmcts.reform.ethos.ecm.consumer.service.MultipleService.CASE_TYPE_ID;
import static uk.gov.hmcts.reform.ethos.ecm.consumer.service.MultipleService.JURISDICTION;

/**
 * Sends update case messages to update-case queue.
 */
@Slf4j
@Component
public class ServiceBusSenderTask {

    private final ServiceBusSender serviceBusSender;

    public ServiceBusSenderTask(
        @Qualifier("update-case-send-helper") ServiceBusSender serviceBusSender) {
        this.serviceBusSender = serviceBusSender;
    }

    @SchedulerLock(name = "service-bus-sender-task")
    @Scheduled(fixedDelayString = "3000")
    public void run() {
        log.info("Started sending update case messages to update-case queue");

        AtomicInteger successCount = new AtomicInteger(0);

        List<UpdateCaseMsg> updateCaseMsgList = getUpdateCaseMsgList();

        updateCaseMsgList
            .forEach(msg -> {
                try {
                    serviceBusSender.sendMessage(msg);
                    logMessageSent(msg);
                    successCount.incrementAndGet();
                } catch (Exception exc) {
                    // log error and try with another message.
                    log.error("Error sending messages to update-case queue", exc);
                }
            });

        log.info(
            "Finished sending messages to update-case queue. Successful: {}. Failures {}.",
            successCount.get(),
            updateCaseMsgList.size() - successCount.get()
        );
    }

    private void logMessageSent(UpdateCaseMsg msg) {
        log.info(
            "Sent msg with multipleRef {}. ethosCaseReference {}, totalCases {}, jurisdiction {}, caseTypeId {}, username {}",
            msg.getMultipleRef(),
            msg.getEthosCaseReference(),
            msg.getTotalCases(),
            msg.getJurisdiction(),
            msg.getCaseTypeId(),
            msg.getUsername()
        );
    }

    private List<UpdateCaseMsg> getUpdateCaseMsgList() {
        UpdateCaseMsg updateCaseMsg1 = UpdateCaseMsg.builder()
            .msgId("1")
            .multipleRef("4150001")
            .ethosCaseReference("4150001/2020")
            .totalCases("3")
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE_ID)
            .username("eric.ccdcooper@gmail.com")
            .build();
        UpdateCaseMsg updateCaseMsg2 = UpdateCaseMsg.builder()
            .msgId("2")
            .multipleRef("4150002")
            .ethosCaseReference("4150002/2020")
            .totalCases("3")
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE_ID)
            .username("eric.ccdcooper@gmail.com")
            .build();
        UpdateCaseMsg updateCaseMsg3 = UpdateCaseMsg.builder()
            .msgId("3")
            .multipleRef("4150003")
            .ethosCaseReference("4150003/2020")
            .totalCases("3")
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE_ID)
            .username("eric.ccdcooper@gmail.com")
            .build();
        return new ArrayList<>(Arrays.asList(updateCaseMsg1, updateCaseMsg2, updateCaseMsg3));
    }
}
