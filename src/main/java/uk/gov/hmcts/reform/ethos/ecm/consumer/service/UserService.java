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

    private final transient IdamApi idamApi;
    private final transient AccessTokenService accessTokenService;

    @Value("${caseWorkerUserName}")
    private transient String caseWorkerUserName;
    @Value("${caseWorkerPassword}")
    private transient String caseWorkerPassword;

    @Autowired
    public UserService(IdamApi idamApi, AccessTokenService accessTokenService) {
        this.idamApi = idamApi;
        this.accessTokenService = accessTokenService;
    }

    @Override
    public UserDetails getUserDetails(String authorisation) {
        return idamApi.retrieveUserDetails(authorisation);
    }

    @Override
    public UserDetails getUserDetailsById(String var1, String var2) {
        return idamApi.getUserByUserId(var1, var2);
    }

    public String getAccessToken() {
        return accessTokenService.getAccessToken(caseWorkerUserName, caseWorkerPassword);
    }

}
