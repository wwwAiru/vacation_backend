package ru.egartech.vacationbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Data
@ConfigurationProperties(prefix = "vacation")
public class ClickUpListIdConfiguration {
    private Map<Integer, Map<String, String>> lists;
}
