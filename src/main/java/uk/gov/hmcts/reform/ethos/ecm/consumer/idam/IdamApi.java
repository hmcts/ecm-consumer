package uk.gov.hmcts.reform.ethos.ecm.consumer.idam;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;

@FeignClient(name = "idam-api", url = "${idam.api.url}")
public interface IdamApi {
    @GetMapping(value = "/o/userinfo")
    UserDetails retrieveUserDetails(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation);

    @GetMapping("/api/v1/users/{userId}")
    UserDetails getUserByUserId(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @PathVariable("userId") String userId
    );
}
