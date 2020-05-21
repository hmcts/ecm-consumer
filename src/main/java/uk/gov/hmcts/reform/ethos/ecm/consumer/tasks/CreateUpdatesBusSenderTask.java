package uk.gov.hmcts.reform.ethos.ecm.consumer.tasks;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus.ServiceBusSender;

import static uk.gov.hmcts.reform.ethos.ecm.consumer.service.MultipleService.CASE_TYPE_ID;
import static uk.gov.hmcts.reform.ethos.ecm.consumer.service.MultipleService.JURISDICTION;

/**
 * Sends create updates messages to create-updates queue.
 */
@Slf4j
@Component
public class CreateUpdatesBusSenderTask {

    private final ServiceBusSender serviceBusSender;

    public CreateUpdatesBusSenderTask(
        @Qualifier("create-updates-send-helper") ServiceBusSender serviceBusSender) {
        this.serviceBusSender = serviceBusSender;
    }

    @SchedulerLock(name = "create-updates-bus-sender-task")
    //@Scheduled(fixedDelayString = "3000")
    @Scheduled(fixedDelay = 100000000, initialDelay = 200000)
    public void run() {
        log.info("Started sending messages to create-updates queue");

        AtomicInteger successCount = new AtomicInteger(0);

        List<UpdateCaseMsg> updateCaseMsgList = getUpdateCaseMsgList();

        updateCaseMsgList
            .forEach(msg -> {
                try {
                    log.info("Start sending messages to CREATE-UPDATES-SEND QUEUE");
                    serviceBusSender.sendMessage(msg);
                    logMessageSent(msg);
                    successCount.incrementAndGet();
                } catch (Exception exc) {
                    // log error and try with another message.
                    log.error("Error sending messages to create-updates queue", exc);
                }
            });

        log.info(
            "Finished sending messages to create-updates queue. Successful: {}. Failures {}.",
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
            .msgId(UUID.randomUUID().toString())
            .multipleRef("4150001")
            .ethosCaseReference("4150001/2020")
            .totalCases("3")
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE_ID)
            .username("eric.ccdcooper@gmail.com")
            .build();
        UpdateCaseMsg updateCaseMsg2 = UpdateCaseMsg.builder()
            .msgId(UUID.randomUUID().toString())
            .multipleRef("4150002")
            .ethosCaseReference("4150002/2020")
            .totalCases("3")
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE_ID)
            .username("eric.ccdcooper@gmail.com")
            .build();
        UpdateCaseMsg updateCaseMsg3 = UpdateCaseMsg.builder()
            .msgId(UUID.randomUUID().toString())
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
