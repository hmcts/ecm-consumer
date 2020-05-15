package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CreateUpdatesService {

    //@Scheduled(fixedRate = 100000)
    public void createUpdates() {
        log.info("Running createUpdates job....");
    }

}
