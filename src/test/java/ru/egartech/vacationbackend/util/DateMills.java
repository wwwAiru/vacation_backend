package ru.egartech.vacationbackend.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateMills {
    public static Long of(String dateTime){
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }
}
