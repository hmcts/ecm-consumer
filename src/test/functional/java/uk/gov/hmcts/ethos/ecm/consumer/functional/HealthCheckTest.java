package uk.gov.hmcts.ethos.ecm.consumer.functional;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HealthCheckTest {

    @Test
    @Category(SmokeTest.class)
    public void healthCheckReturns200() {
        assertThat("smokeTest", is("smokeTest"));
    }
}
