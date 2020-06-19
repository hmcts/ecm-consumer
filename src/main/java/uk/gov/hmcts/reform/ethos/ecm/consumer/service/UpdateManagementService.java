package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.MultipleErrors;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.repository.MultipleCounterRepository;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.repository.MultipleErrorsRepository;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class UpdateManagementService {

    private final MultipleCounterRepository multipleCounterRepository;
    private final MultipleErrorsRepository multipleErrorsRepository;
    private final MultipleUpdateService multipleUpdateService;
    private final SingleUpdateService singleUpdateService;
    private final EmailService emailService;

    @Autowired
    public UpdateManagementService(MultipleCounterRepository multipleCounterRepository,
                                   MultipleErrorsRepository multipleErrorsRepository,
                                   MultipleUpdateService multipleUpdateService,
                                   SingleUpdateService singleUpdateService,
                                   EmailService emailService) {
        this.multipleCounterRepository = multipleCounterRepository;
        this.multipleErrorsRepository = multipleErrorsRepository;
        this.multipleUpdateService = multipleUpdateService;
        this.singleUpdateService = singleUpdateService;
        this.emailService = emailService;
    }

    public void updateLogic(UpdateCaseMsg updateCaseMsg) throws IOException {

        singleUpdateService.sendUpdateToSingleLogic(updateCaseMsg);

        checkIfFinish(updateCaseMsg);

    }

    private void checkIfFinish(UpdateCaseMsg updateCaseMsg) throws IOException {

        //TODO REMOVE
        multipleCounterRepository.deleteAllByMultipleref(updateCaseMsg.getMultipleRef());

        int counter = multipleCounterRepository.persistentQGetNextMultipleCountVal(updateCaseMsg.getMultipleRef());
        log.info("COUNTER: " + counter + " TOTAL CASES: " + updateCaseMsg.getTotalCases());

        if (counter == Integer.parseInt(updateCaseMsg.getTotalCases())) {

            multipleUpdateService.sendUpdateToMultipleLogic(updateCaseMsg);

            sendEmailToUser(updateCaseMsg);

            deleteMultipleRefDatabase(updateCaseMsg.getMultipleRef());
        }

    }

    private void sendEmailToUser(UpdateCaseMsg updateCaseMsg) {

        List<MultipleErrors> multipleErrorsList = multipleErrorsRepository.findByMultipleref(updateCaseMsg.getMultipleRef());

        if (multipleErrorsList != null && !multipleErrorsList.isEmpty()) {

            log.info("Sending email to user: With errors");
            emailService.sendConfirmationErrorEmail(updateCaseMsg.getUsername(), multipleErrorsList);

        } else {

            log.info("Sending email to user: No errors");
            emailService.sendConfirmationEmail(updateCaseMsg.getUsername());

        }

    }

    private void deleteMultipleRefDatabase(String multipleRef) {

        log.info("Clearing all multipleRef");
        multipleCounterRepository.deleteAllByMultipleref(multipleRef);
        //multipleErrorsRepository.deleteAllByMultipleref(multipleRef);
    }

}
