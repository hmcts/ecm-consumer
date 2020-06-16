package uk.gov.hmcts.ethos.ecm.consumer.helpers;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.ethos.ecm.consumer.helpers.CreateUpdatesHelper;
import uk.gov.hmcts.reform.ethos.ecm.consumer.model.servicebus.CreateUpdatesMsg;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CreateUpdatesHelperTest {

    private List<String> ethosCaseRefCollection;

    @Before
    public void setUp() {
        ethosCaseRefCollection = Arrays.asList("4150001/2020", "4150002/2020", "4150003/2020", "4150004/2020");
    }

    @Test
    public void generateUpdateCaseMsg() {
        List<CreateUpdatesMsg> createUpdatesMsgList = CreateUpdatesHelper.getCreateUpdatesMessagesCollection(ethosCaseRefCollection);
        assertEquals(2, createUpdatesMsgList.size());
    }

}
