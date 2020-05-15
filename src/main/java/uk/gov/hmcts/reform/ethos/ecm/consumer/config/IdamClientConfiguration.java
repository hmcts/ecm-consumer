package uk.gov.hmcts.reform.ethos.ecm.consumer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ethos.ecm.consumer.idam.IdamApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.OAuth2Configuration;

@Configuration
public class IdamClientConfiguration {

    @Bean
    public IdamClient idamClient(IdamApi idamApi, OAuth2Configuration oauth2Configuration) {
        return new IdamClient(idamApi, oauth2Configuration);
    }
}
