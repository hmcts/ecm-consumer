package uk.gov.hmcts.ethos.ecm.consumer.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

public class UserServiceTest {

    @InjectMocks
    private UserService userService;
    @Mock
    private IdamApi idamApi;
    @Mock
    private OAuth2Configuration oAuth2Configuration;
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
            public ApiAccessToken loginUser(String userName, String password) {
                return HelperTest.getApiAccessToken();
            }
            @Override
            public TokenResponse generateOpenIdToken(TokenRequest tokenRequest) { return getTokenResponse(); };
        };
        userService = new UserService(idamApi, oAuth2Configuration);
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
        String token = userService.getAccessToken("Username", "Password");
        assertEquals("Bearer accessToken", token);
    }
}
