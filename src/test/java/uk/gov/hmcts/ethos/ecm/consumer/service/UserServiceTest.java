package uk.gov.hmcts.ethos.ecm.consumer.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ethos.ecm.consumer.helpers.HelperTest;
import uk.gov.hmcts.reform.ethos.ecm.consumer.config.OAuth2Configuration;
import uk.gov.hmcts.reform.ethos.ecm.consumer.idam.ApiAccessToken;
import uk.gov.hmcts.reform.ethos.ecm.consumer.idam.IdamApi;
import uk.gov.hmcts.reform.ethos.ecm.consumer.idam.TokenRequest;
import uk.gov.hmcts.reform.ethos.ecm.consumer.idam.TokenResponse;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.UserService;
import java.util.Collections;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;
    @Mock
    private IdamApi idamApi;
    @Mock
    private OAuth2Configuration oAuth2Configuration;
    @Mock
    private RestTemplate restTemplate;
    private UserDetails userDetails;

    @Before
    public void setUp() {
        userDetails = getUserDetails();
        oAuth2Configuration = new OAuth2Configuration("redirectUri", "id", "secret");
        idamApi = new IdamApi() {
            @Override
            public UserDetails retrieveUserDetails(String authorisation) {
                return getUserDetails();
            }
            @Override
            public ResponseEntity<ApiAccessToken> loginUser(String userName, String password) {
                return new ResponseEntity<>(HelperTest.getApiAccessToken(), HttpStatus.OK);
            }
//            @Override
//            public ResponseEntity<TokenResponse> generateOpenIdToken(TokenRequest tokenRequest) {
//                return new ResponseEntity<>(getTokenResponse(), HttpStatus.OK);
//            }
        };
        userService = new UserService(idamApi, oAuth2Configuration, restTemplate);
    }

    private UserDetails getUserDetails() {
        UserDetails userDetails = new UserDetails();
        userDetails.setUid("id");
        userDetails.setEmail("mail@mail.com");
        userDetails.setFirstName("Mike");
        userDetails.setLastName("Jordan");
        userDetails.setRoles(Collections.singletonList("role"));
        return userDetails;
    }

    private TokenResponse getTokenResponse() {
        return new TokenResponse("accessToken", "expiresIn",
                                 "idToken", "refreshToken", "scope", "tokenType");
    }

    @Test
    public void shouldCheckAllUserDetails() {
        assertEquals("mail@mail.com", userService.getUserDetails("TOKEN").getEmail());
        assertEquals("Mike", userService.getUserDetails("TOKEN").getFirstName());
        assertEquals("Jordan", userService.getUserDetails("TOKEN").getLastName());
        assertEquals(Collections.singletonList("role"), userService.getUserDetails("TOKEN").getRoles());
        assertEquals(userDetails.toString(), userService.getUserDetails("TOKEN").toString());
    }

    @Test
    public void loginUserTest() {
        ApiAccessToken apiAccessToken = userService.loginUser("Username", "Password");
        assertEquals("Access token", apiAccessToken.getAccessToken());
        assertEquals("Api Auth Token", apiAccessToken.getApiAuthToken());
        assertEquals("Expires In", apiAccessToken.getExpiresIn());
        assertEquals("Id Token", apiAccessToken.getIdToken());
        assertEquals("Refresh Token", apiAccessToken.getRefreshToken());
        assertEquals("Scope", apiAccessToken.getScope());
        assertEquals("Token Type", apiAccessToken.getTokenType());
    }

    @Test
    public void getAccessTokenTest() {
        String url = "http://sidam-api:5000/o/token";
        ReflectionTestUtils.setField(userService,"idamApiOIDCUrl", url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<TokenResponse> responseEntity = new ResponseEntity<>(HttpStatus.OK);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(getTokenRequestMap(), headers);
        when(restTemplate.postForEntity(eq(url), eq(httpEntity), eq(TokenResponse.class))).thenReturn(responseEntity);
        String token = userService.getAccessToken("Username", "Password");
        assertEquals("", token);
    }

    private TokenRequest getTokenRequest() {
        return new TokenRequest("id", "secret", "password", "redirectUri",
                             "Username", "Password", "openid", null, null);
    }

    private MultiValueMap<String, String> getTokenRequestMap() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", "id");
        map.add("client_secret", "secret");
        map.add("grant_type", "password");
        map.add("redirect_uri", "redirectUri");
        map.add("username", "Username");
        map.add("password", "Password");
        map.add("scope", "openid");
        map.add("refresh_token", null);
        map.add("code", null);
        return map;
    }

    @Test
    public void tokenRequestAndResponseTest() {
        TokenRequest tokenRequest = getTokenRequest();
        assertEquals("id", tokenRequest.getClientId());
        assertEquals("secret", tokenRequest.getClientSecret());
        assertEquals("password", tokenRequest.getGrantType());
        assertEquals("Password", tokenRequest.getPassword());
        assertEquals("redirectUri", tokenRequest.getRedirectUri());
        assertEquals("openid", tokenRequest.getScope());
        assertEquals("Username", tokenRequest.getUsername());
        assertNull(tokenRequest.getRefreshToken());
        assertNull(tokenRequest.getCode());
        TokenResponse tokenResponse = getTokenResponse();
        assertEquals("accessToken", tokenResponse.accessToken);
        assertEquals("expiresIn", tokenResponse.expiresIn);
        assertEquals("idToken", tokenResponse.idToken);
        assertEquals("refreshToken", tokenResponse.refreshToken);
        assertEquals("scope", tokenResponse.scope);
        assertEquals("tokenType", tokenResponse.tokenType);
    }

}
