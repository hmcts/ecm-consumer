package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UpdateCaseListenService {

    @Value("${queue.create-updates.listen.connection-string}")
    private String queueCreateUpdatesListenString;
    @Value("${queue.create-updates.queue-name}")
    private String queueCreateUpdatesName;

    @Value("${queue.update-case.listen.connection-string}")
    private String queueUpdateCaseSendString;
    @Value("${queue.update-case.send.connection-string}")
    private String queueUpdateCaseListenString;
    @Value("${queue.update-case.queue-name}")
    private String queueUpdateCaseName;

    @Autowired
    public UpdateCaseListenService() {

    }

    //@Scheduled(fixedRate = 100000)
    public void updateCaseListen() {
        log.info("Running updateCaseSend job....");
        log.info("queueCreateUpdatesListenString" + queueCreateUpdatesListenString);
        log.info("queueCreateUpdatesName" + queueCreateUpdatesName);
        log.info("queueUpdateCaseListenString" + queueUpdateCaseListenString);
        log.info("queueUpdateCaseSendString" + queueUpdateCaseSendString);
        log.info("queueUpdateCaseName" + queueUpdateCaseName);
    }

}
