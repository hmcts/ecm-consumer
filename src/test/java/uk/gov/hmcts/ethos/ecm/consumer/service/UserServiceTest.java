package uk.gov.hmcts.ethos.ecm.consumer.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.reform.ethos.ecm.consumer.idam.IdamApi;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.AccessTokenService;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.UserService;
import java.util.Collections;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;
    @Mock
    private IdamApi idamApi;
    @Mock
    private AccessTokenService accessTokenService;
    private UserDetails userDetails;

    @Before
    public void setUp() {
        userDetails = getUserDetails();
        idamApi = authorisation -> getUserDetails();
        userService = new UserService(idamApi, accessTokenService);
        ReflectionTestUtils.setField(userService, "caseWorkerUserName", "example@gmail.com");
        ReflectionTestUtils.setField(userService, "caseWorkerPassword", "123456");
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
    public void getAccessToken() {
        when(accessTokenService.getAccessToken(anyString(), anyString())).thenReturn("accessToken");
        assertEquals("accessToken", userService.getAccessToken());
    }

}
