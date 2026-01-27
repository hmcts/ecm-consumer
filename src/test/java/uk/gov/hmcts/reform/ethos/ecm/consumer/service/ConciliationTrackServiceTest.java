package uk.gov.hmcts.reform.ethos.ecm.consumer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ecm.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.ecm.common.model.ccd.types.JurCodesType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_FAST_TRACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_NO_CONCILIATION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_OPEN_TRACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_STANDARD_TRACK;

@ExtendWith(MockitoExtension.class)
class ConciliationTrackServiceTest {

    private ConciliationTrackService conciliationTrackService;
    private SubmitEvent submitEvent;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        conciliationTrackService = new ConciliationTrackService();
        submitEvent = new SubmitEvent();
        caseData = new CaseData();
        submitEvent.setCaseData(caseData);
    }

    @Test
    void testPopulateConciliationTrack_WithOpenTrackCode() {
        List<JurCodesTypeItem> jurCodesCollection = new ArrayList<>();
        jurCodesCollection.add(createJurCodeItem("DAG"));
        caseData.setJurCodesCollection(jurCodesCollection);

        conciliationTrackService.populateConciliationTrackForJurisdiction(submitEvent);

        assertThat(caseData.getConciliationTrack()).isEqualTo(CONCILIATION_TRACK_OPEN_TRACK);
    }

    @Test
    void testPopulateConciliationTrack_WithMultipleOpenTrackCodes() {
        List<JurCodesTypeItem> jurCodesCollection = new ArrayList<>();
        jurCodesCollection.add(createJurCodeItem("DDA"));
        jurCodesCollection.add(createJurCodeItem("DSO"));
        jurCodesCollection.add(createJurCodeItem("EQP"));
        caseData.setJurCodesCollection(jurCodesCollection);

        conciliationTrackService.populateConciliationTrackForJurisdiction(submitEvent);

        assertThat(caseData.getConciliationTrack()).isEqualTo(CONCILIATION_TRACK_OPEN_TRACK);
    }

    @Test
    void testPopulateConciliationTrack_WithStandardTrackCode() {
        List<JurCodesTypeItem> jurCodesCollection = new ArrayList<>();
        jurCodesCollection.add(createJurCodeItem("ADG"));
        caseData.setJurCodesCollection(jurCodesCollection);

        conciliationTrackService.populateConciliationTrackForJurisdiction(submitEvent);

        assertThat(caseData.getConciliationTrack()).isEqualTo(CONCILIATION_TRACK_STANDARD_TRACK);
    }

    @Test
    void testPopulateConciliationTrack_WithMultipleStandardTrackCodes() {
        List<JurCodesTypeItem> jurCodesCollection = new ArrayList<>();
        jurCodesCollection.add(createJurCodeItem("APA"));
        jurCodesCollection.add(createJurCodeItem("AWR"));
        jurCodesCollection.add(createJurCodeItem("FTE"));
        caseData.setJurCodesCollection(jurCodesCollection);

        conciliationTrackService.populateConciliationTrackForJurisdiction(submitEvent);

        assertThat(caseData.getConciliationTrack()).isEqualTo(CONCILIATION_TRACK_STANDARD_TRACK);
    }

    @Test
    void testPopulateConciliationTrack_WithFastTrackCode() {
        List<JurCodesTypeItem> jurCodesCollection = new ArrayList<>();
        jurCodesCollection.add(createJurCodeItem("BOC"));
        caseData.setJurCodesCollection(jurCodesCollection);

        conciliationTrackService.populateConciliationTrackForJurisdiction(submitEvent);

        assertThat(caseData.getConciliationTrack()).isEqualTo(CONCILIATION_TRACK_FAST_TRACK);
    }

    @Test
    void testPopulateConciliationTrack_WithMultipleFastTrackCodes() {
        List<JurCodesTypeItem> jurCodesCollection = new ArrayList<>();
        jurCodesCollection.add(createJurCodeItem("FML"));
        jurCodesCollection.add(createJurCodeItem("PAY"));
        jurCodesCollection.add(createJurCodeItem("TIP"));
        caseData.setJurCodesCollection(jurCodesCollection);

        conciliationTrackService.populateConciliationTrackForJurisdiction(submitEvent);

        assertThat(caseData.getConciliationTrack()).isEqualTo(CONCILIATION_TRACK_FAST_TRACK);
    }

    @Test
    void testPopulateConciliationTrack_WithNoConciliationCode() {
        List<JurCodesTypeItem> jurCodesCollection = new ArrayList<>();
        jurCodesCollection.add(createJurCodeItem("ADT"));
        caseData.setJurCodesCollection(jurCodesCollection);

        conciliationTrackService.populateConciliationTrackForJurisdiction(submitEvent);

        assertThat(caseData.getConciliationTrack()).isEqualTo(CONCILIATION_TRACK_NO_CONCILIATION);
    }

    @Test
    void testPopulateConciliationTrack_WithMultipleNoConciliationCodes() {
        List<JurCodesTypeItem> jurCodesCollection = new ArrayList<>();
        jurCodesCollection.add(createJurCodeItem("CCP"));
        jurCodesCollection.add(createJurCodeItem("COM"));
        jurCodesCollection.add(createJurCodeItem("EAP"));
        caseData.setJurCodesCollection(jurCodesCollection);

        conciliationTrackService.populateConciliationTrackForJurisdiction(submitEvent);

        assertThat(caseData.getConciliationTrack()).isEqualTo(CONCILIATION_TRACK_NO_CONCILIATION);
    }

    @Test
    void testPopulateConciliationTrack_OpenTrackTakesPrecedenceOverStandard() {
        List<JurCodesTypeItem> jurCodesCollection = new ArrayList<>();
        jurCodesCollection.add(createJurCodeItem("ADG")); // ST
        jurCodesCollection.add(createJurCodeItem("DDA")); // OP
        jurCodesCollection.add(createJurCodeItem("AWR")); // ST
        caseData.setJurCodesCollection(jurCodesCollection);

        conciliationTrackService.populateConciliationTrackForJurisdiction(submitEvent);

        assertThat(caseData.getConciliationTrack()).isEqualTo(CONCILIATION_TRACK_OPEN_TRACK);
    }

    @Test
    void testPopulateConciliationTrack_OpenTrackTakesPrecedenceOverFast() {
        List<JurCodesTypeItem> jurCodesCollection = new ArrayList<>();
        jurCodesCollection.add(createJurCodeItem("BOC")); // SH
        jurCodesCollection.add(createJurCodeItem("DAG")); // OP
        jurCodesCollection.add(createJurCodeItem("FML")); // SH
        caseData.setJurCodesCollection(jurCodesCollection);

        conciliationTrackService.populateConciliationTrackForJurisdiction(submitEvent);

        assertThat(caseData.getConciliationTrack()).isEqualTo(CONCILIATION_TRACK_OPEN_TRACK);
    }

    @Test
    void testPopulateConciliationTrack_OpenTrackTakesPrecedenceOverNo() {
        List<JurCodesTypeItem> jurCodesCollection = new ArrayList<>();
        jurCodesCollection.add(createJurCodeItem("ADT")); // NO
        jurCodesCollection.add(createJurCodeItem("DRB")); // OP
        jurCodesCollection.add(createJurCodeItem("CCP")); // NO
        caseData.setJurCodesCollection(jurCodesCollection);

        conciliationTrackService.populateConciliationTrackForJurisdiction(submitEvent);

        assertThat(caseData.getConciliationTrack()).isEqualTo(CONCILIATION_TRACK_OPEN_TRACK);
    }

    @Test
    void testPopulateConciliationTrack_StandardTrackTakesPrecedenceOverFast() {
        List<JurCodesTypeItem> jurCodesCollection = new ArrayList<>();
        jurCodesCollection.add(createJurCodeItem("BOC")); // SH
        jurCodesCollection.add(createJurCodeItem("AWR")); // ST
        jurCodesCollection.add(createJurCodeItem("PAY")); // SH
        caseData.setJurCodesCollection(jurCodesCollection);

        conciliationTrackService.populateConciliationTrackForJurisdiction(submitEvent);

        assertThat(caseData.getConciliationTrack()).isEqualTo(CONCILIATION_TRACK_STANDARD_TRACK);
    }

    @Test
    void testPopulateConciliationTrack_StandardTrackTakesPrecedenceOverNo() {
        List<JurCodesTypeItem> jurCodesCollection = new ArrayList<>();
        jurCodesCollection.add(createJurCodeItem("ADT")); // NO
        jurCodesCollection.add(createJurCodeItem("FTE")); // ST
        jurCodesCollection.add(createJurCodeItem("COM")); // NO
        caseData.setJurCodesCollection(jurCodesCollection);

        conciliationTrackService.populateConciliationTrackForJurisdiction(submitEvent);

        assertThat(caseData.getConciliationTrack()).isEqualTo(CONCILIATION_TRACK_STANDARD_TRACK);
    }

    @Test
    void testPopulateConciliationTrack_FastTrackTakesPrecedenceOverNo() {
        List<JurCodesTypeItem> jurCodesCollection = new ArrayList<>();
        jurCodesCollection.add(createJurCodeItem("ADT")); // NO
        jurCodesCollection.add(createJurCodeItem("FML")); // SH
        jurCodesCollection.add(createJurCodeItem("CCP")); // NO
        caseData.setJurCodesCollection(jurCodesCollection);

        conciliationTrackService.populateConciliationTrackForJurisdiction(submitEvent);

        assertThat(caseData.getConciliationTrack()).isEqualTo(CONCILIATION_TRACK_FAST_TRACK);
    }

    @Test
    void testPopulateConciliationTrack_WithAllTrackTypes() {
        List<JurCodesTypeItem> jurCodesCollection = new ArrayList<>();
        jurCodesCollection.add(createJurCodeItem("COM")); // NO
        jurCodesCollection.add(createJurCodeItem("BOC")); // SH
        jurCodesCollection.add(createJurCodeItem("AWR")); // ST
        jurCodesCollection.add(createJurCodeItem("DDA")); // OP
        caseData.setJurCodesCollection(jurCodesCollection);

        conciliationTrackService.populateConciliationTrackForJurisdiction(submitEvent);

        assertThat(caseData.getConciliationTrack()).isEqualTo(CONCILIATION_TRACK_OPEN_TRACK);
    }

    @Test
    void testPopulateConciliationTrack_WithEmptyCollection() {
        caseData.setJurCodesCollection(new ArrayList<>());

        conciliationTrackService.populateConciliationTrackForJurisdiction(submitEvent);

        assertThat(caseData.getConciliationTrack()).isNull();
    }

    @Test
    void testPopulateConciliationTrack_WithNullCollection() {
        caseData.setJurCodesCollection(null);

        conciliationTrackService.populateConciliationTrackForJurisdiction(submitEvent);

        assertThat(caseData.getConciliationTrack()).isNull();
    }

    @Test
    void testPopulateConciliationTrack_WithUnrecognizedCode() {
        List<JurCodesTypeItem> jurCodesCollection = new ArrayList<>();
        jurCodesCollection.add(createJurCodeItem("UNKNOWN"));
        caseData.setJurCodesCollection(jurCodesCollection);

        conciliationTrackService.populateConciliationTrackForJurisdiction(submitEvent);

        assertThat(caseData.getConciliationTrack()).isNull();
    }

    @Test
    void testPopulateConciliationTrack_WithMultipleUnrecognizedCodes() {
        List<JurCodesTypeItem> jurCodesCollection = new ArrayList<>();
        jurCodesCollection.add(createJurCodeItem("ABC"));
        jurCodesCollection.add(createJurCodeItem("XYZ"));
        jurCodesCollection.add(createJurCodeItem("123"));
        caseData.setJurCodesCollection(jurCodesCollection);

        conciliationTrackService.populateConciliationTrackForJurisdiction(submitEvent);

        assertThat(caseData.getConciliationTrack()).isNull();
    }

    @Test
    void testPopulateConciliationTrack_AllOpenTrackCodes() {
        String[] openTrackCodes = {"DAG", "DDA", "DRB", "DSO", "EQP", "GRA", "MAT", "PID", "RRD", "SXD", "VIC"};
        for (String code : openTrackCodes) {
            setUp();
            List<JurCodesTypeItem> jurCodesCollection = new ArrayList<>();
            jurCodesCollection.add(createJurCodeItem(code));
            caseData.setJurCodesCollection(jurCodesCollection);

            conciliationTrackService.populateConciliationTrackForJurisdiction(submitEvent);

            assertThat(caseData.getConciliationTrack())
                    .as("Code %s should map to OPEN_TRACK", code)
                    .isEqualTo(CONCILIATION_TRACK_OPEN_TRACK);
        }
    }

    @Test
    void testPopulateConciliationTrack_AllStandardTrackCodes() {
        String[] standardTrackCodes = {"ADG", "APA", "AWR", "DOD", "FCT", "FLW", "FTE", "FT1", "FPI", "FWP", "FWS",
            "HSD", "HSR", "IRF", "MWD", "PAC", "PLD", "PTE", "RTR(ST)", "SUN",
            "TPE", "TT", "TUE", "TUI", "TUM", "TUR", "TUS", "TXC(ST)", "UDC", "UDL", "UIA", "WTR"};
        for (String code : standardTrackCodes) {
            setUp();
            List<JurCodesTypeItem> jurCodesCollection = new ArrayList<>();
            jurCodesCollection.add(createJurCodeItem(code));
            caseData.setJurCodesCollection(jurCodesCollection);

            conciliationTrackService.populateConciliationTrackForJurisdiction(submitEvent);

            assertThat(caseData.getConciliationTrack())
                    .as("Code %s should map to STANDARD_TRACK", code)
                    .isEqualTo(CONCILIATION_TRACK_STANDARD_TRACK);
        }
    }

    @Test
    void testPopulateConciliationTrack_AllFastTrackCodes() {
        String[] fastTrackCodes = {"BOC", "FML", "FPA", "FTC", "FTO", "FTP", "FTR", "FTS", "FTU", "PAY",
            "RPT", "TIP", "WA", "WTR(AL)"};
        Arrays.stream(fastTrackCodes).forEach(code -> {
            setUp();
            List<JurCodesTypeItem> jurCodesCollection = new ArrayList<>();
            jurCodesCollection.add(createJurCodeItem(code));
            caseData.setJurCodesCollection(jurCodesCollection);
            conciliationTrackService.populateConciliationTrackForJurisdiction(submitEvent);
            assertThat(caseData.getConciliationTrack())
                .as("Code %s should map to FAST_TRACK", code)
                .isEqualTo(CONCILIATION_TRACK_FAST_TRACK);
        });
    }

    @Test
    void testPopulateConciliationTrack_AllNoConciliationCodes() {
        String[] noConciliationCodes = {"ADT", "ADT(ST)", "CCP", "COM", "EAP", "HAS", "ISV", "LEV ", "LSO", "MWA",
            "NNA", "PEN", "RPT(S)", "RTR", "TXC", "WTA"};
        for (String code : noConciliationCodes) {
            setUp();
            List<JurCodesTypeItem> jurCodesCollection = new ArrayList<>();
            jurCodesCollection.add(createJurCodeItem(code));
            caseData.setJurCodesCollection(jurCodesCollection);

            conciliationTrackService.populateConciliationTrackForJurisdiction(submitEvent);

            assertThat(caseData.getConciliationTrack())
                    .as("Code %s should map to NO_CONCILIATION", code)
                    .isEqualTo(CONCILIATION_TRACK_NO_CONCILIATION);
        }
    }

    private JurCodesTypeItem createJurCodeItem(String code) {
        JurCodesTypeItem item = new JurCodesTypeItem();
        JurCodesType type = new JurCodesType();
        type.setJuridictionCodesList(code);
        item.setValue(type);
        return item;
    }
}
