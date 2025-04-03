package uk.gov.hmcts.reform.ethos.ecm.consumer.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.UserService;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REJECTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Component
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class MigrateToReformTask {
    public static final String STATE_KEYWORD = "state.keyword";
    public static final String DATA_CASE_TYPE = "data.caseType";
    private final UserService userService;
    private final CcdClient ccdClient;

    @Value("${cron.caseTypeId}")
    private String caseTypeIdsString;
    @Value("${cron.maxCasesPerSearch}")
    private int maxCases;
    @Value("${multithreading.migrate-to-reform-task.threads}")
    private int threads;
    @Value("${cron.migrateToReformEnabled}")
    private boolean migrateToReformEnabled;

    @Scheduled(cron = "${cron.migrateToReform}")
    public void migrateToReform() {
        if (!migrateToReformEnabled) {
            log.info("Migrate to Reform task is disabled");
            return;
        }
        String query = buildQuery();
        String adminUserToken = userService.getAccessToken();
        String[] caseTypeIds = caseTypeIdsString.split(",");

        Arrays.stream(caseTypeIds).forEach(caseTypeId -> {
            try {
                log.info("Migrating cases for case type: {}", caseTypeId);
                List<SubmitEvent> cases = ccdClient.buildAndGetElasticSearchRequest(adminUserToken, caseTypeId, query);
                log.info("{} - Migrate to Reform task - Retrieved {} cases", caseTypeId, cases.size());
                executeUpdates(caseTypeId, cases, adminUserToken);
                log.info("Migrated cases for case type: {}", caseTypeId);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        });
    }

    private void executeUpdates(String caseTypeId, List<SubmitEvent> cases, String adminUserToken) {
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        AtomicInteger count = new AtomicInteger();

        cases.forEach(submitEvent -> executorService.execute(() -> {
            int currentCount = count.incrementAndGet();
            log.info("Processing case {} of {}.", currentCount, cases.size());
            triggerMigration(submitEvent, adminUserToken, caseTypeId);
        }));
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.error("ExecutorService did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            log.error("Thread execution interrupted: {}", e.getMessage());
        }
    }

    private void triggerMigration(SubmitEvent submitEvent, String adminUserToken, String caseTypeId) {
        try {
            CCDRequest startEventResponse = ccdClient.startEventForCase(
                adminUserToken,
                caseTypeId,
                "EMPLOYMENT",
                String.valueOf(submitEvent.getCaseId()),
                "migrateCase");
            ccdClient.submitEventForCase(
                adminUserToken,
                startEventResponse.getCaseDetails().getCaseData(),
                caseTypeId,
                "EMPLOYMENT",
                startEventResponse,
                String.valueOf(submitEvent.getCaseId()));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private String buildQuery() {
        return new SearchSourceBuilder()
            .size(maxCases)
            .query(new BoolQueryBuilder()
               .should(new BoolQueryBuilder()
                   .must(new TermsQueryBuilder(STATE_KEYWORD, SUBMITTED_STATE, ACCEPTED_STATE, REJECTED_STATE))
                   .must(new MatchQueryBuilder(DATA_CASE_TYPE, SINGLE_CASE_TYPE)))
               .should(new BoolQueryBuilder()
                    .must(new TermsQueryBuilder(STATE_KEYWORD, CLOSED_STATE))
                    .must(new MatchQueryBuilder(DATA_CASE_TYPE, SINGLE_CASE_TYPE))
                    .must(new RangeQueryBuilder("last_state_modified_date")
                          .from("2024-04-01")))
                .should(new BoolQueryBuilder()
                    .must(new TermsQueryBuilder(STATE_KEYWORD, SUBMITTED_STATE, ACCEPTED_STATE,
                                                REJECTED_STATE, CLOSED_STATE))
                    .must(new MatchQueryBuilder(DATA_CASE_TYPE, SINGLE_CASE_TYPE))
                    .must(new TermQueryBuilder("data.additionalCaseInfo.additional_live_appeal", YES)))
            ).toString();
    }
}
