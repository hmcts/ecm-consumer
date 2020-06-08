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
import uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus.CreateUpdatesMsg;
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
    @Scheduled(fixedDelay = 100000000, initialDelay = 200000)
    public void run() {
        log.info("Started sending messages to create-updates queue");

        AtomicInteger successCount = new AtomicInteger(0);

        List<CreateUpdatesMsg> createUpdatesMsgList = getCreateUpdatesMsgList();

        createUpdatesMsgList
            .forEach(msg -> {
                try {
                    //log.info("Start sending messages to CREATE-UPDATES-SEND QUEUE");
                    serviceBusSender.sendMessageAsync(msg);
                    log.info("SENT -----> " + msg.toString());
                    successCount.incrementAndGet();
                } catch (Exception exc) {
                    // log error and try with another message.
                    log.error("Error sending messages to create-updates queue", exc);
                    // IF ERROR SEND BACK TO THE USER
                }
            });

        log.info(
            "Finished sending messages to create-updates queue. Successful: {}. Failures {}.",
            successCount.get(),
            createUpdatesMsgList.size() - successCount.get()
        );
    }

    private List<CreateUpdatesMsg> getCreateUpdatesMsgList() {
        CreateUpdatesMsg createUpdatesMsg1 = CreateUpdatesMsg.builder()
            .msgId(UUID.randomUUID().toString())
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE_ID)
            .multipleRef("4150001")
            .ethosCaseRefCollection(Arrays.asList("4150001/2020", "4150002/2020", "4150003/2020"))
            .totalCases("3")
            .username("eric1.ccdcooper@gmail.com")
            .build();
        CreateUpdatesMsg createUpdatesMsg2 = CreateUpdatesMsg.builder()
            .msgId(UUID.randomUUID().toString())
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE_ID)
            .multipleRef("4150002")
            .ethosCaseRefCollection(Arrays.asList("4150004/2020", "4150005/2020", "4150006/2020"))
            .totalCases("3")
            .username("eric2.ccdcooper@gmail.com")
            .build();
        CreateUpdatesMsg createUpdatesMsg3 = CreateUpdatesMsg.builder()
            .msgId(UUID.randomUUID().toString())
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE_ID)
            .multipleRef("4150003")
            .ethosCaseRefCollection(Arrays.asList("4150007/2020", "4150008/2020", "4150009/2020"))
            .totalCases("3")
            .username("eric3.ccdcooper@gmail.com")
            .build();
        CreateUpdatesMsg createUpdatesMsg4 = CreateUpdatesMsg.builder()
            .msgId(UUID.randomUUID().toString())
            .jurisdiction(JURISDICTION)
            .caseTypeId(CASE_TYPE_ID)
            .multipleRef("4150004")
            .ethosCaseRefCollection(Arrays.asList("4150010/2020", "4150011/2020", "4150012/2020"))
            .totalCases("3")
            .username("eric4.ccdcooper@gmail.com")
            .build();
        return new ArrayList<>(Arrays.asList(createUpdatesMsg1, createUpdatesMsg2, createUpdatesMsg3, createUpdatesMsg4));
    }

}
