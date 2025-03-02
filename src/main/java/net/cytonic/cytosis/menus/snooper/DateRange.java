package net.cytonic.cytosis.menus.snooper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public enum DateRange {
    ONE_HOUR,
    TWELVE_HOURS,
    ONE_DAY,
    SEVEN_DAYS,
    THIRTY_DAYS,
    ONE_HUNDRED_EIGHTY_DAYS,
    ONE_YEAR,
    ALL_TIME;

    public Instant instantValue() {
        return switch (this) {
            case ONE_HOUR -> Instant.now().minus(1, ChronoUnit.HOURS);
            case TWELVE_HOURS -> Instant.now().minus(12, ChronoUnit.HOURS);
            case ONE_DAY -> Instant.now().minus(1, ChronoUnit.DAYS);
            case SEVEN_DAYS -> Instant.now().minus(7, ChronoUnit.DAYS);
            case THIRTY_DAYS -> Instant.now().minus(30, ChronoUnit.DAYS);
            case ONE_HUNDRED_EIGHTY_DAYS -> Instant.now().minus(180, ChronoUnit.DAYS);
            case ONE_YEAR -> Instant.now().minus(365, ChronoUnit.DAYS);
            case ALL_TIME -> Instant.EPOCH; // effectively all time :)
        };
    }

    public DateRange next() {
        return this.ordinal() + 1 < values().length ? values()[this.ordinal() + 1] : ONE_HOUR;
    }

    public DateRange previous() {
        return this.ordinal() - 1 >= 0 ? values()[this.ordinal() - 1] : ALL_TIME;
    }
}
