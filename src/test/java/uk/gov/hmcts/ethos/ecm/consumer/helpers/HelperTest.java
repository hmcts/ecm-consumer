package uk.gov.hmcts.ethos.ecm.consumer.helpers;

import org.junit.Test;
import uk.gov.hmcts.reform.ethos.ecm.consumer.helpers.Helper;

import static org.junit.Assert.assertEquals;

public class HelperTest {

    @Test
    public void formatLocalDateTest() {
        assertEquals("2 February 2020", Helper.formatLocalDate("2020-02-02T11:02:11.000"));
    }

}
