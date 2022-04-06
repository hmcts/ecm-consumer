package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.ResetStateDataModel;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.MultipleCounter;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.MultipleErrors;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.repository.MultipleCounterRepository;
import uk.gov.hmcts.reform.ethos.ecm.consumer.domain.repository.MultipleErrorsRepository;

import java.io.IOException;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.reform.ethos.ecm.consumer.helpers.Constants.UNPROCESSABLE_MESSAGE;

@Slf4j
@RequiredArgsConstructor
@Service
public class UpdateManagementService {

    private final MultipleCounterRepository multipleCounterRepository;
    private final MultipleErrorsRepository multipleErrorsRepository;
    private final MultipleUpdateService multipleUpdateService;
    private final SingleReadingService singleReadingService;
    private final EmailService emailService;

    public void updateLogic(UpdateCaseMsg updateCaseMsg) throws IOException, InterruptedException {

        if (updateCaseMsg.getDataModelParent() instanceof ResetStateDataModel) {

            log.info("Resetting state of multiple to Open State");

            deleteMultipleRefDatabase(updateCaseMsg.getMultipleRef());

        } else {

            singleReadingService.sendUpdateToSingleLogic(updateCaseMsg);

            if (!updateCaseMsg.getMultipleRef().equals(SINGLE_CASE_TYPE)) {

                checkIfFinish(updateCaseMsg);

            }

        }

    }

    public void checkIfFinish(UpdateCaseMsg updateCaseMsg) throws IOException, InterruptedException {

        int counter = getNextCounterNumber(updateCaseMsg.getMultipleRef());

        log.info("COUNTER: " + counter + " TOTAL CASES: " + updateCaseMsg.getTotalCases());

        if (counter == Integer.parseInt(updateCaseMsg.getTotalCases())) {

            log.info("----- MULTIPLE UPDATE FINISHED: sending update to multiple ------");

            if (updateCaseMsg.getConfirmation().equals(YES)) {

                List<MultipleErrors> multipleErrorsList =
                    multipleErrorsRepository.findByMultipleref(updateCaseMsg.getMultipleRef());

                multipleUpdateService.sendUpdateToMultipleLogic(updateCaseMsg, multipleErrorsList);

                sendEmailToUser(updateCaseMsg, multipleErrorsList);

            }

            deleteMultipleRefDatabase(updateCaseMsg.getMultipleRef());
        }

    }

    private int getNextCounterNumber(String multipleRef) {
        return multipleCounterRepository.persistentQGetNextMultipleCountVal(multipleRef);
    }

    private void sendEmailToUser(UpdateCaseMsg updateCaseMsg, List<MultipleErrors> multipleErrorsList) {

        if (multipleErrorsList != null && !multipleErrorsList.isEmpty()) {

            emailService.sendConfirmationErrorEmail(updateCaseMsg.getUsername(),
                                                    multipleErrorsList, updateCaseMsg.getMultipleRef());

        } else {

            emailService.sendConfirmationEmail(updateCaseMsg.getUsername(), updateCaseMsg.getMultipleRef());

        }

    }

    private void deleteMultipleRefDatabase(String multipleRef) {

        log.info("Clearing all multipleRef from DBs: " + multipleRef);

        log.info("Clearing multiple counter repository");
        List<MultipleCounter> multipleCounters = multipleCounterRepository.findByMultipleref(multipleRef);
        multipleCounterRepository.deleteInBatch(multipleCounters);

        log.info("Clearing multiple errors repository");
        List<MultipleErrors> multipleErrors = multipleErrorsRepository.findByMultipleref(multipleRef);
        multipleErrorsRepository.deleteInBatch(multipleErrors);

        log.info("Deleted repositories");
    }

    public void addUnrecoverableErrorToDatabase(UpdateCaseMsg updateCaseMsg) {

        multipleErrorsRepository.persistentQLogMultipleError(updateCaseMsg.getMultipleRef(),
                                                             updateCaseMsg.getEthosCaseReference(),
                                                             UNPROCESSABLE_MESSAGE);
    }
}
