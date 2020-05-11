package uk.gov.hmcts.ethos.ecm.consumer.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ethos.ecm.consumer.helpers.HelperTest;
import uk.gov.hmcts.reform.ethos.ecm.consumer.idam.ApiAccessToken;
import uk.gov.hmcts.reform.ethos.ecm.consumer.idam.IdamApi;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.UserService;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class UserServiceTest {

    @InjectMocks
    private UserService userService;
    @Mock
    private IdamApi idamApi;

    private UserDetails userDetails;

    @Before
    public void setUp() {
        userDetails = getUserDetails();
        idamApi = new IdamApi() {
            @Override
            public UserDetails retrieveUserDetails(String authorisation) {
                return getUserDetails();
            }

            @Override
            public ApiAccessToken loginUser(String userName, String password) {
                return HelperTest.getApiAccessToken();
            }
        };
        userService = new UserService(idamApi);
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
}
