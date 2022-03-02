package uk.gov.hmcts.reform.ethos.ecm.consumer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {
    @Bean
    public OpenAPI api() {
        return new OpenAPI()
            .info(new Info().title("ECM Consumer API")
                      .description("Message consumer for ECM")
                      .version("v0.0.1"));
    }
}
