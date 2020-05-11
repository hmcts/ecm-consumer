package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.reform.ethos.ecm.consumer.idam.ApiAccessToken;
import uk.gov.hmcts.reform.ethos.ecm.consumer.idam.IdamApi;

@Component
public class UserService implements uk.gov.hmcts.ecm.common.service.UserService {

    private final IdamApi idamApi;

    @Autowired
    public UserService(IdamApi idamApi) {
        this.idamApi = idamApi;
    }

    public UserDetails getUserDetails(String authorisation) {
        return idamApi.retrieveUserDetails(authorisation);
    }

    public ApiAccessToken loginUser(String userName, String password) {
        return idamApi.loginUser(userName, password);
    }

}
