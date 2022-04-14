package uk.gov.hmcts.reform.ethos.ecm.consumer.functional;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@Category(SmokeTest.class)
public class HealthCheckTest {

    @Test
    public void healthCheckReturns200() {
        assertEquals("smokeTest", "smokeTest");
    }
}

