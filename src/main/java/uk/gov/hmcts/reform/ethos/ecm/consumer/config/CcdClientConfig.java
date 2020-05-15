package uk.gov.hmcts.reform.ethos.ecm.consumer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.*;

@Slf4j
@Component
public class CcdClientConfig {

    private static final String START_EVENT_FOR_CASE_URL_CASEWORKER_FORMAT =
            "%s/caseworkers/%s/jurisdictions/%s/case-types/%s/cases/%s/event-triggers/%s/token";
    private static final String SUBMIT_EVENT_FOR_URL_CASEWORKER_FORMAT =
            "%s/caseworkers/%s/jurisdictions/%s/case-types/%s/cases/%s/events";
    private static final String SEARCH_CASES_FORMAT = "%s/searchCases?%s";

    @Value("${ccd.data.store.api.url}")
    private String ccdDataStoreApiBaseUrl;

    String buildRetrieveCasesUrlElasticSearch(String ctid) {
        String param = "ctid=" + ctid;
        log.info("Format: " + String.format(SEARCH_CASES_FORMAT, ccdDataStoreApiBaseUrl, param));
        return String.format(SEARCH_CASES_FORMAT, ccdDataStoreApiBaseUrl, param);
    }

    String buildStartEventForBulkCaseUrl(String uid, String jid, String ctid, String cid) {
        return String.format(START_EVENT_FOR_CASE_URL_CASEWORKER_FORMAT, ccdDataStoreApiBaseUrl, uid, jid, ctid, cid, UPDATE_BULK_EVENT_TRIGGER_ID);
    }

    String buildSubmitEventForCaseUrl(String uid, String jid, String ctid, String cid) {
        return String.format(SUBMIT_EVENT_FOR_URL_CASEWORKER_FORMAT, ccdDataStoreApiBaseUrl, uid, jid, ctid, cid);
    }
}
