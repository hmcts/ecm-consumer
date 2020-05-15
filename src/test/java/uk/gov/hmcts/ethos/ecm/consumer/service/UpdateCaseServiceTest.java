package uk.gov.hmcts.ethos.ecm.consumer.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.UpdateCaseService;

@RunWith(SpringJUnit4ClassRunner.class)
public class UpdateCaseServiceTest {

    @InjectMocks
    private UpdateCaseService updateCaseService;

    @Before
    public void setUp() {
    }

    @Test
    public void updateCase() {
        updateCaseService.updateCase();
    }

}
