package uk.gov.hmcts.reform.ethos.ecm.consumer.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.helpers.ESHelper;
import uk.gov.hmcts.ecm.common.model.bulk.BulkCaseSearchResult;
import uk.gov.hmcts.ecm.common.model.bulk.BulkData;
import uk.gov.hmcts.ecm.common.model.bulk.SubmitBulkEvent;
import uk.gov.hmcts.ecm.common.model.ccd.*;
import uk.gov.hmcts.ecm.common.service.UserService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CcdClient {

    private RestTemplate restTemplate;
    private UserService userService;
    private CcdClientConfig ccdClientConfig;
    private CaseDataBuilder caseDataBuilder;

    private AuthTokenGenerator authTokenGenerator;
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    static final String UPDATE_BULK_EVENT_SUMMARY = "Bulk case updated by bulk";

    public CcdClient(RestTemplate restTemplate, UserService userService, CaseDataBuilder caseDataBuilder,
                     CcdClientConfig ccdClientConfig, AuthTokenGenerator authTokenGenerator) {
        this.restTemplate = restTemplate;
        this.userService = userService;
        this.ccdClientConfig = ccdClientConfig;
        this.authTokenGenerator = authTokenGenerator;
        this.caseDataBuilder = caseDataBuilder;
    }

    public List<SubmitBulkEvent> retrieveBulkCasesElasticSearch(String authToken, String caseTypeId, String multipleReference) throws IOException {
        List<SubmitBulkEvent> submitBulkEvents = new ArrayList<>();
        log.info("QUERY: " + ESHelper.getBulkSearchQuery(multipleReference));
        HttpEntity<String> request =
                new HttpEntity<>(ESHelper.getBulkSearchQuery(multipleReference), buildHeaders(authToken));
        //log.info("REQUEST: " + request);
        String url = ccdClientConfig.buildRetrieveCasesUrlElasticSearch(caseTypeId);
        BulkCaseSearchResult bulkCaseSearchResult = restTemplate.exchange(url, HttpMethod.POST, request, BulkCaseSearchResult.class).getBody();
        if (bulkCaseSearchResult != null && bulkCaseSearchResult.getCases() != null) {
            submitBulkEvents.addAll(bulkCaseSearchResult.getCases());
        }
        return submitBulkEvents;
    }

    public CCDRequest startBulkEventForCase(String authToken, String caseTypeId, String jurisdiction, String cid) throws IOException {
        HttpEntity<String> request =
                new HttpEntity<>(buildHeaders(authToken));
        String uri = ccdClientConfig.buildStartEventForBulkCaseUrl(userService.getUserDetails(authToken).getUid(), jurisdiction,
                caseTypeId, cid);
        return restTemplate.exchange(uri, HttpMethod.GET, request, CCDRequest.class).getBody();
    }

    public SubmitBulkEvent submitBulkEventForCase(String authToken, BulkData bulkData, String caseTypeId, String jurisdiction, CCDRequest req, String cid)
            throws IOException {
        HttpEntity<CaseDataContent> request =
                new HttpEntity<>(caseDataBuilder.buildBulkDataContent(bulkData, req, UPDATE_BULK_EVENT_SUMMARY), buildHeaders(authToken));
        String uri = ccdClientConfig.buildSubmitEventForCaseUrl(userService.getUserDetails(authToken).getUid(), jurisdiction,
                caseTypeId, cid);
        return restTemplate.exchange(uri, HttpMethod.POST, request, SubmitBulkEvent.class).getBody();
    }

    HttpHeaders buildHeaders(String authToken) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        if (!authToken.matches("[a-zA-Z0-9._\\s\\S]+$")) {
            throw new IOException("authToken regex exception");
        }
        log.info("AuthToken: " + authToken);
        headers.add(HttpHeaders.AUTHORIZATION, authToken);
        String serviceToken = authTokenGenerator.generate();
        log.info("ServiceToken: " + serviceToken);
        headers.add(SERVICE_AUTHORIZATION, serviceToken);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
        log.info("Headers: " + headers.toString());
        return headers;
    }

}
