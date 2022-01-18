package uk.gov.hmcts.reform.ethos.ecm.consumer.idam;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class TokenResponseTest {

    private TokenResponse getTokenResponse() {
        return new TokenResponse("accessToken", "expiresIn",
                                 "idToken", "refreshToken", "scope", "tokenType");
    }

    @Test
    public void tokenResponseTest() {
        TokenResponse tokenResponse = getTokenResponse();
        assertEquals("accessToken", tokenResponse.accessToken);
        assertEquals("expiresIn", tokenResponse.expiresIn);
        assertEquals("idToken", tokenResponse.idToken);
        assertEquals("refreshToken", tokenResponse.refreshToken);
        assertEquals("scope", tokenResponse.scope);
        assertEquals("tokenType", tokenResponse.tokenType);
    }
}
