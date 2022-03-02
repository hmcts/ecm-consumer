package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.ethos.ecm.consumer.config.OAuth2Configuration;
import uk.gov.hmcts.reform.ethos.ecm.consumer.idam.TokenResponse;

@Slf4j
@Component
public class AccessTokenService {

    private final transient OAuth2Configuration oauth2Configuration;
    private final transient RestTemplate restTemplate;

    public static final String OPENID_GRANT_TYPE = "password";
    public static final String OPENID_SCOPE = "openid profile roles";
    public static final String BEARER_AUTH_TYPE = "Bearer";
    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String GRANT_TYPE = "grant_type";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String SCOPE = "scope";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String CODE = "code";

    @Value("${idam.api.url.oidc}")
    private transient String idamApiOidcUrl;

    @Autowired
    public AccessTokenService(OAuth2Configuration oauth2Configuration, RestTemplate restTemplate) {
        this.oauth2Configuration = oauth2Configuration;
        this.restTemplate = restTemplate;
    }

    public String getAccessToken(String username, String password) {
        ResponseEntity<TokenResponse> responseEntity
            = restTemplate.postForEntity(idamApiOidcUrl,
                                         new HttpEntity<>(getTokenRequest(username, password), getHeaders()),
                                         TokenResponse.class);
        if (responseEntity.getBody() != null) {
            var tokenResponse = responseEntity.getBody();
            if (tokenResponse != null && tokenResponse.accessToken != null) {
                return BEARER_AUTH_TYPE + " " + tokenResponse.accessToken;
            }
        }
        return "";
    }

    private HttpHeaders getHeaders() {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private MultiValueMap<String, String> getTokenRequest(String username, String password) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(CLIENT_ID, oauth2Configuration.getClientId());
        map.add(CLIENT_SECRET, oauth2Configuration.getClientSecret());
        map.add(GRANT_TYPE, OPENID_GRANT_TYPE);
        map.add(REDIRECT_URI, oauth2Configuration.getRedirectUri());
        map.add(USERNAME, username);
        map.add(PASSWORD, password);
        map.add(SCOPE, OPENID_SCOPE);
        map.add(REFRESH_TOKEN, null);
        map.add(CODE, null);
        return map;
    }

}
