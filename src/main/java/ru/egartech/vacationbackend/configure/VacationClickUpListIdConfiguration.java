package ru.egartech.vacationbackend.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Data
@ConfigurationProperties(prefix = "vacation")
public class VacationClickUpListIdConfiguration {
    private Map<Integer, Map<String, String>> lists;
}
