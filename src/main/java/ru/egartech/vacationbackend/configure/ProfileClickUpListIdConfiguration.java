package ru.egartech.vacationbackend.configure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Data
@ConfigurationProperties(prefix = "profile")
public class ProfileClickUpListIdConfiguration {
    private Map<Integer, Map<String, String>> lists;
}
