package uk.gov.hmcts.reform.ethos.ecm.consumer.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ecm.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.UserService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.LONDON_CENTRAL_CASE_TYPE_ID;

@Component
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class UpdateCasesTask {
    private static final String LONDON_CENTRAL_VENUE = "London Central";
    private static final String LONDON_TRIBUNALS_CENTRE_VENUE = "London Tribunals Centre";
    private static final String NO_CASES_TO_UPDATE = "No cases to update";
    private final UserService userService;
    private final CcdClient ccdClient;
    @Value("${cron.updateCasesEnabled}")
    private boolean updateCasesEnabled;
    @Value("${cron.updateCaseList}")
    private String caseListToUpdate;

    @Scheduled(cron = "${cron.updateCases}")
    public void updateCases() {
        log.info("Update cases task is running");
        if (!updateCasesEnabled) {
            log.info("Update cases task is disabled");
            return;
        }
        log.info("Update cases task is enabled");
        if (isNullOrEmpty(caseListToUpdate)) {
            log.info(NO_CASES_TO_UPDATE);
            return;
        }
        List<String> caseList = List.of(caseListToUpdate.split(","));
        if (caseList.isEmpty()) {
            log.info(NO_CASES_TO_UPDATE);
            return;
        }
        log.info("Number of cases to update - {}", caseList.size());
        String adminUserToken = userService.getAccessToken();
        AtomicInteger count = new AtomicInteger();
        AtomicReference<String> updatedCases = new AtomicReference<>();
        List<SubmitEvent> casesToUpdate;

        try {
            casesToUpdate = ccdClient.retrieveCasesElasticSearch(
            adminUserToken,
            LONDON_CENTRAL_CASE_TYPE_ID,
            caseList);
        } catch (IOException e) {
            log.error("Error retrieving cases", e);
            return;
        }

        if (casesToUpdate.isEmpty()) {
            log.info(NO_CASES_TO_UPDATE);
            return;
        }

        casesToUpdate.stream()
            .parallel()
            .forEach(submitEvent -> {
                try {
                    log.info("Updating case {}", submitEvent.getCaseId());
                    triggerUpdateCaseEvent(adminUserToken, submitEvent.getCaseId(), count, updatedCases);

                } catch (Exception e) {
                    log.error("Error updating case {}", submitEvent, e);
                }
            });

        log.info("Updates completed for {} cases", count.get());
        log.info("Updated cases cases: {}", updatedCases.get());

    }

    private void triggerUpdateCaseEvent(String adminUserToken, long caseId, AtomicInteger count,
                                        AtomicReference<String> updatedCases) throws IOException {
        CCDRequest ccdRequest = ccdClient.startEventForCase(
            adminUserToken,
            LONDON_CENTRAL_CASE_TYPE_ID,
            "EMPLOYMENT",
            String.valueOf(caseId));
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        emptyIfNull(caseData.getHearingCollection()).stream()
            .filter(hearing -> !ObjectUtils.isEmpty(hearing))
            .map(HearingTypeItem::getValue)
            .forEach(hearingItem -> hearingItem.getHearingDateCollection().stream()
                .map(DateListedTypeItem::getValue)
                .filter(UpdateCasesTask::isValidVenue)
                .forEach(hearingDateCollection -> {
                    hearingDateCollection.setHearingVenueDay(LONDON_TRIBUNALS_CENTRE_VENUE);
                    hearingItem.setHearingVenue(LONDON_TRIBUNALS_CENTRE_VENUE);
                }));

        ccdClient.submitEventForCase(
            adminUserToken,
            caseData,
            LONDON_CENTRAL_CASE_TYPE_ID,
            "EMPLOYMENT",
            ccdRequest,
            String.valueOf(caseId)
        );

        count.incrementAndGet();
        updatedCases.updateAndGet(v -> v == null
            ? caseData.getEthosCaseReference()
            : v + ", " + caseData.getEthosCaseReference());
        log.info("Updated case {}", caseId);

    }

    private static boolean isValidVenue(DateListedType hearingDateCollection) {
        return LONDON_CENTRAL_VENUE.equals(defaultIfEmpty(hearingDateCollection.getHearingVenueDay(), "venue"))
               && LocalDateTime.parse(hearingDateCollection.getListedDate()).isAfter(LocalDateTime.now())
               && HEARING_STATUS_LISTED.equals(hearingDateCollection.getHearingStatus());
    }
}
