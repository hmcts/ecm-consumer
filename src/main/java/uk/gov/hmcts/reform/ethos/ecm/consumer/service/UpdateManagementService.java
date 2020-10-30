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

import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.reform.ethos.ecm.consumer.helpers.Constants.UNPROCESSABLE_MESSAGE;

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

    public void updateLogic(UpdateCaseMsg updateCaseMsg) throws IOException, InterruptedException {

        singleUpdateService.sendUpdateToSingleLogic(updateCaseMsg);

        checkIfFinish(updateCaseMsg);

    }

    public void checkIfFinish(UpdateCaseMsg updateCaseMsg) throws IOException, InterruptedException {

        int counter = getNextCounterNumberWithDelay(updateCaseMsg.getMultipleRef());

        log.info("COUNTER: " + counter + " TOTAL CASES: " + updateCaseMsg.getTotalCases());

        if (counter == Integer.parseInt(updateCaseMsg.getTotalCases())) {

            log.info("----- MULTIPLE UPDATE FINISHED: sending update to multiple ------");

            List<MultipleErrors> multipleErrorsList = multipleErrorsRepository.findByMultipleref(updateCaseMsg.getMultipleRef());

            if (updateCaseMsg.getConfirmation().equals(YES)) {

                multipleUpdateService.sendUpdateToMultipleLogic(updateCaseMsg, multipleErrorsList);

                sendEmailToUser(updateCaseMsg, multipleErrorsList);

            }

            deleteMultipleRefDatabase(updateCaseMsg.getMultipleRef());
        }

    }

    private int getNextCounterNumberWithDelay(String multipleRef) throws InterruptedException {

        long delay = (long)(Math.random() * 1000);

        log.info("Delaying... " + delay);

        Thread.sleep(delay);

        return multipleCounterRepository.persistentQGetNextMultipleCountVal(multipleRef);

    }

    private void sendEmailToUser(UpdateCaseMsg updateCaseMsg, List<MultipleErrors> multipleErrorsList) {

        if (multipleErrorsList != null && !multipleErrorsList.isEmpty()) {

            emailService.sendConfirmationErrorEmail(updateCaseMsg.getUsername(), multipleErrorsList, updateCaseMsg.getMultipleRef());

        } else {

            emailService.sendConfirmationEmail(updateCaseMsg.getUsername(), updateCaseMsg.getMultipleRef());

        }

    }

    private void deleteMultipleRefDatabase(String multipleRef) {

        log.info("Clearing all multipleRef from DBs");

        multipleCounterRepository.deleteAllByMultipleref(multipleRef);
        multipleErrorsRepository.deleteAllByMultipleref(multipleRef);

    }

    public void addUnrecoverableErrorToDatabase(UpdateCaseMsg updateCaseMsg) {

        multipleErrorsRepository.persistentQLogMultipleError(updateCaseMsg.getMultipleRef(),
                                                             updateCaseMsg.getEthosCaseReference(),
                                                             UNPROCESSABLE_MESSAGE);
    }
}
