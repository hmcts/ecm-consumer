package uk.gov.hmcts.reform.ethos.ecm.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"uk.gov.hmcts.reform.ethos", "uk.gov.hmcts.ecm.common",
    "uk.gov.hmcts.reform.authorisation", "uk.gov.hmcts.reform.ccd"})
@EnableFeignClients(basePackages =
    {"uk.gov.hmcts.reform.ethos.ecm"
    })
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
@Slf4j
@EnableScheduling
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
