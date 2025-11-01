package uk.gov.hmcts.reform.ethos.ecm.consumer.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ecm.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.ecm.common.model.ccd.types.HearingType;
import uk.gov.hmcts.reform.ethos.ecm.consumer.service.UserService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCasesTaskTest {

    private static final String AUTH_TOKEN = "AuthToken";
    private static final String EMPLOYMENT = "EMPLOYMENT";
    private static final String CASE_NUMBER = "1800001/2020";
    private static final String CASE_LIST_TO_UPDATE = "caseListToUpdate";

    @Mock
    private UserService userService;

    @Mock
    private CcdClient ccdClient;

    private UpdateCasesTask updateCasesTask;

    @BeforeEach
    void setUp() {
        updateCasesTask = new UpdateCasesTask(userService, ccdClient);
        ReflectionTestUtils.setField(updateCasesTask, "updateCasesEnabled", true);
        lenient().when(userService.getAccessToken()).thenReturn(AUTH_TOKEN);
    }

    @Test
    void should_do_nothing_when_feature_toggle_off() {
        ReflectionTestUtils.setField(updateCasesTask, "updateCasesEnabled", false);
        ReflectionTestUtils.setField(updateCasesTask, CASE_LIST_TO_UPDATE, "1234567890123456");

        updateCasesTask.updateCases();

        verifyNoInteractions(userService);
        verifyNoInteractions(ccdClient);
    }

    @Test
    void should_do_nothing_when_no_case_list_configured() {
        ReflectionTestUtils.setField(updateCasesTask, CASE_LIST_TO_UPDATE, "");

        updateCasesTask.updateCases();

        verifyNoMoreInteractions(ccdClient);
    }

    @Test
    void should_return_when_retrieve_cases_throws_io_exception() throws IOException {
        ReflectionTestUtils.setField(updateCasesTask, CASE_LIST_TO_UPDATE, CASE_NUMBER);
        when(ccdClient.retrieveCasesElasticSearch(eq(AUTH_TOKEN), anyString(), anyList()))
            .thenThrow(new IOException("boom"));

        updateCasesTask.updateCases();

        verify(ccdClient, times(1)).retrieveCasesElasticSearch(eq(AUTH_TOKEN),
            anyString(), eq(List.of(CASE_NUMBER)));
        verify(ccdClient, times(0)).startEventForCase(anyString(), anyString(), anyString(), anyString());
        verify(ccdClient, times(0)).submitEventForCase(anyString(), any(), anyString(), anyString(),
            any(), anyString());
    }

    @Test
    void should_return_when_no_cases_to_update() throws IOException {
        ReflectionTestUtils.setField(updateCasesTask, CASE_LIST_TO_UPDATE, CASE_NUMBER);
        when(ccdClient.retrieveCasesElasticSearch(eq(AUTH_TOKEN), anyString(), anyList()))
            .thenReturn(Collections.emptyList());

        updateCasesTask.updateCases();

        verify(ccdClient, times(1)).retrieveCasesElasticSearch(eq(AUTH_TOKEN), anyString(),
            eq(List.of(CASE_NUMBER)));
        verify(ccdClient, times(0)).startEventForCase(anyString(), anyString(), anyString(), anyString());
        verify(ccdClient, times(0)).submitEventForCase(anyString(), any(), anyString(), anyString(), any(),
            anyString());
    }

    @Test
    void should_update_case_when_hearing_venue_is_london_central_and_in_future_and_listed() throws IOException {
        // given
        long caseId = 1234567890123456L;
        ReflectionTestUtils.setField(updateCasesTask, CASE_LIST_TO_UPDATE, String.valueOf(caseId));

        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseId(caseId);
        when(ccdClient.retrieveCasesElasticSearch(eq(AUTH_TOKEN), anyString(), anyList()))
            .thenReturn(List.of(submitEvent));

        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference(CASE_NUMBER);
        caseData.setHearingCollection(buildHearingCollection(true));
        CCDRequest ccdRequest = buildCcdRequest(caseData);

        when(ccdClient.startEventForCase(eq(AUTH_TOKEN), anyString(), eq(EMPLOYMENT), eq(String.valueOf(caseId))))
            .thenReturn(ccdRequest);

        ArgumentCaptor<CaseData> caseDataCaptor = ArgumentCaptor.forClass(CaseData.class);

        // when
        updateCasesTask.updateCases();

        // then
        verify(ccdClient, times(1)).startEventForCase(eq(AUTH_TOKEN), anyString(), eq(EMPLOYMENT),
            eq(String.valueOf(caseId)));
        verify(ccdClient, times(1)).submitEventForCase(eq(AUTH_TOKEN), caseDataCaptor.capture(), anyString(),
            eq(EMPLOYMENT), eq(ccdRequest), eq(String.valueOf(caseId)));

        CaseData submitted = caseDataCaptor.getValue();
        HearingTypeItem hearingTypeItem = submitted.getHearingCollection().get(0);
        HearingType hearing = hearingTypeItem.getValue();
        // hearing venue on the day and overall hearing venue should be updated to LTC
        assertThat(hearing.getHearingVenue()).isEqualTo("London Tribunals Centre");
        DateListedTypeItem dayItem = hearing.getHearingDateCollection().get(0);
        assertThat(dayItem.getValue().getHearingVenueDay()).isEqualTo("London Tribunals Centre");
    }

    @Test
    void should_not_update_hearing_when_not_matching_criteria() throws IOException {
        // given
        long caseId = 9876543210000000L;
        ReflectionTestUtils.setField(updateCasesTask, CASE_LIST_TO_UPDATE, String.valueOf(caseId));

        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseId(caseId);
        when(ccdClient.retrieveCasesElasticSearch(eq(AUTH_TOKEN), anyString(), anyList()))
            .thenReturn(List.of(submitEvent));

        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("1800002/2020");
        caseData.setHearingCollection(buildHearingCollection(false));
        CCDRequest ccdRequest = buildCcdRequest(caseData);

        when(ccdClient.startEventForCase(eq(AUTH_TOKEN), anyString(), eq(EMPLOYMENT), eq(String.valueOf(caseId))))
            .thenReturn(ccdRequest);

        ArgumentCaptor<CaseData> caseDataCaptor = ArgumentCaptor.forClass(CaseData.class);

        // when
        updateCasesTask.updateCases();

        // then
        verify(ccdClient, times(1)).submitEventForCase(eq(AUTH_TOKEN), caseDataCaptor.capture(), anyString(),
            eq(EMPLOYMENT), eq(ccdRequest), eq(String.valueOf(caseId)));

        CaseData submitted = caseDataCaptor.getValue();
        HearingTypeItem hearingTypeItem = submitted.getHearingCollection().get(0);
        HearingType hearing = hearingTypeItem.getValue();
        // No change expected
        assertThat(hearing.getHearingVenue()).isEqualTo("Original Venue");
        DateListedTypeItem dayItem = hearing.getHearingDateCollection().get(0);
        assertThat(dayItem.getValue().getHearingVenueDay()).isEqualTo("London Central");
    }

    @Test
    void should_handle_null_hearing_collection() throws IOException {
        // given
        long caseId = 1122334455667788L;
        ReflectionTestUtils.setField(updateCasesTask, CASE_LIST_TO_UPDATE, String.valueOf(caseId));

        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseId(caseId);
        when(ccdClient.retrieveCasesElasticSearch(eq(AUTH_TOKEN), anyString(), anyList()))
            .thenReturn(List.of(submitEvent));

        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("1800003/2020");
        caseData.setHearingCollection(null); // should be safely handled
        CCDRequest ccdRequest = buildCcdRequest(caseData);
        when(ccdClient.startEventForCase(eq(AUTH_TOKEN), anyString(), eq(EMPLOYMENT), eq(String.valueOf(caseId))))
            .thenReturn(ccdRequest);

        // when
        updateCasesTask.updateCases();

        // then
        verify(ccdClient, times(1)).submitEventForCase(eq(AUTH_TOKEN), any(CaseData.class), anyString(),
            eq(EMPLOYMENT), eq(ccdRequest), eq(String.valueOf(caseId)));
    }

    private CCDRequest buildCcdRequest(CaseData caseData) {
        CaseDetails details = new CaseDetails();
        details.setCaseData(caseData);
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(details);
        return ccdRequest;
    }

    private List<HearingTypeItem> buildHearingCollection(boolean validForUpdate) {
        HearingType hearing = new HearingType();
        if (validForUpdate) {
            hearing.setHearingVenue(null); // will be set to LTC
        } else {
            hearing.setHearingVenue("Original Venue");
        }

        DateListedType day = new DateListedType();
        day.setHearingVenueDay("London Central");
        if (validForUpdate) {
            day.setListedDate(LocalDateTime.now().plusDays(1).toString());
            day.setHearingStatus("Listed");
        } else {
            day.setListedDate(LocalDateTime.now().minusDays(1).toString());
            day.setHearingStatus("Postponed");
        }
        DateListedTypeItem dayItem = new DateListedTypeItem();
        dayItem.setValue(day);

        List<DateListedTypeItem> days = new ArrayList<>();
        days.add(dayItem);

        hearing.setHearingDateCollection(days);
        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setValue(hearing);
        List<HearingTypeItem> hearings = new ArrayList<>();
        hearings.add(hearingTypeItem);
        return hearings;
    }
}
