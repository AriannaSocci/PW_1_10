package org.ancora_casini.PW_1_10.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.temporal.ChronoUnit;

@AllArgsConstructor
@Getter
public enum TimeUnit {
    MINUTES(ChronoUnit.MINUTES),
    HOURS(ChronoUnit.HOURS),
    DAYS(ChronoUnit.DAYS),
    WEEKS(ChronoUnit.WEEKS),
    MONTHS(ChronoUnit.MONTHS);

    private final ChronoUnit chronoUnit;

}
