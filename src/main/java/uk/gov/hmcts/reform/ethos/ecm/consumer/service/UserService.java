package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.reform.ethos.ecm.consumer.idam.IdamApi;

@Slf4j
@Component
public class UserService implements uk.gov.hmcts.ecm.common.service.UserService {

    private final IdamApi idamApi;
    private final AccessTokenService accessTokenService;

    @Value("${caseWorkerUserName}")
    private String caseWorkerUserName;
    @Value("${caseWorkerPassword}")
    private String caseWorkerPassword;

    @Autowired
    public UserService(IdamApi idamApi, AccessTokenService accessTokenService) {
        this.idamApi = idamApi;
        this.accessTokenService = accessTokenService;
    }

    public UserDetails getUserDetails(String authorisation) {
        return idamApi.retrieveUserDetails(authorisation);
    }

    public String getAccessToken() {
        log.info("UserName: " + caseWorkerUserName);
        log.info("Password: " + caseWorkerPassword);
        return accessTokenService.getAccessToken(caseWorkerUserName, caseWorkerPassword);
    }

}
