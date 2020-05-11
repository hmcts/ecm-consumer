package uk.gov.hmcts.reform.ethos.ecm.consumer.helpers;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_DATE_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN;

@Slf4j
public class Helper {

    public static String formatLocalDate(String date) {
        return !isNullOrEmpty(date) ? LocalDate.parse(date, OLD_DATE_TIME_PATTERN).format(NEW_DATE_PATTERN) : "";
    }
}
