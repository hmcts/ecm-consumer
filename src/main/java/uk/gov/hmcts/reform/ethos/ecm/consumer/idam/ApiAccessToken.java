package uk.gov.hmcts.reform.ethos.ecm.consumer.idam;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

/**
 * @author Ivano
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiAccessToken {

    @JsonProperty(value = "access_token")
    private String accessToken;

    @JsonProperty(value = "refresh_token")
    private String refreshToken;

    @JsonProperty(value = "scope")
    private String scope;

    @JsonProperty(value = "id_token")
    private String idToken;

    @JsonProperty(value = "token_type")
    private String tokenType;

    @JsonProperty(value = "expires_in")
    private String expiresIn;

    @JsonProperty(value = "api_auth_token")
    private String apiAuthToken;
}
