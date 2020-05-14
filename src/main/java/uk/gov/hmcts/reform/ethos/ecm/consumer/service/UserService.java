package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.reform.ethos.ecm.consumer.config.OAuth2Configuration;
import uk.gov.hmcts.reform.ethos.ecm.consumer.idam.ApiAccessToken;
import uk.gov.hmcts.reform.ethos.ecm.consumer.idam.IdamApi;
import uk.gov.hmcts.reform.ethos.ecm.consumer.idam.TokenRequest;

@Component
public class UserService implements uk.gov.hmcts.ecm.common.service.UserService {

    public static final String OPENID_GRANT_TYPE = "password";
    public static final String OPENID_SCOPE = "openid";
    public static final String BEARER_AUTH_TYPE = "Bearer";

    private final IdamApi idamApi;
    private OAuth2Configuration oauth2Configuration;

    @Autowired
    public UserService(IdamApi idamApi, OAuth2Configuration oauth2Configuration) {
        this.idamApi = idamApi;
        this.oauth2Configuration = oauth2Configuration;
    }

    public UserDetails getUserDetails(String authorisation) {
        return idamApi.retrieveUserDetails(authorisation);
    }

    public ApiAccessToken loginUser(String userName, String password) {
        ResponseEntity<ApiAccessToken> responseEntity = idamApi.loginUser(userName, password);
        return responseEntity != null? responseEntity.getBody() : null;
    }

    public String getAccessToken(String username, String password) {
        TokenRequest tokenRequest =
            new TokenRequest(
                oauth2Configuration.getClientId(),
                oauth2Configuration.getClientSecret(),
                OPENID_GRANT_TYPE,
                oauth2Configuration.getRedirectUri(),
                username,
                password,
                OPENID_SCOPE,
                null,
                null
            );
        return BEARER_AUTH_TYPE + " " + idamApi.generateOpenIdToken(tokenRequest).accessToken;
    }

}
