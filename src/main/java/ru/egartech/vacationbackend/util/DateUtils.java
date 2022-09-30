package ru.egartech.vacationbackend.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAccessor;

public class DateUtils {
    public static LocalDate toLocalDate(Long timeInMills){
        return LocalDate.ofInstant(Instant.ofEpochMilli(timeInMills), ZoneId.systemDefault());
    }

    public static Long toMills(TemporalAccessor moment){
        return Instant.from(moment).toEpochMilli();
    }
}
