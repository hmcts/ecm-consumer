package uk.gov.hmcts.reform.ethos.ecm.consumer.idam;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;

@FeignClient(name = "idam-api", url = "${idam.api.url}")
public interface IdamApi extends uk.gov.hmcts.reform.idam.client.IdamApi {
//    @GetMapping(value = "/o/userinfo")
//    UserDetails retrieveUserDetails(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation);
//
//    @PostMapping(value = "/loginUser")
//    ResponseEntity<ApiAccessToken> loginUser(@RequestParam("userName") String userName,
//                             @RequestParam("password") String password);

//    @PostMapping(value = "/o/token")
//    ResponseEntity<TokenResponse> generateOpenIdToken(@RequestBody TokenRequest tokenRequest);
}
