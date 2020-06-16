package uk.gov.hmcts.reform.ethos.ecm.consumer.tasks;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.ethos.ecm.consumer.helpers.CreateUpdatesHelper;
import uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus.CreateUpdatesMsg;
import uk.gov.hmcts.reform.ethos.ecm.consumer.servicebus.ServiceBusSender;

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

        List<String> ethosCaseRefCollection = Arrays.asList("4150002/2020");

        List<CreateUpdatesMsg> createUpdatesMsgList = CreateUpdatesHelper.getCreateUpdatesMessagesCollection(ethosCaseRefCollection);

        createUpdatesMsgList
            .forEach(msg -> {
                try {
                    serviceBusSender.sendMessage(msg);
                    log.info("SENT -----> " + msg.toString());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // log error and try with another message.
                    log.error("Error sending messages to create-updates queue", e);
                    //TODO IF ERROR SEND BACK TO THE USER
                }
            });

        log.info(
            "Finished sending messages to create-updates queue. Successful: {}. Failures {}.",
            successCount.get(),
            createUpdatesMsgList.size() - successCount.get()
        );
    }

}
