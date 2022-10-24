package ru.egartech.vacationbackend.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Data
@ConfigurationProperties(prefix = "settings.vacation")
public class VacationProperty {
    private Map<Integer, Item> lists;
    @Data
    public static class Item{
        private String startDate;
        private String endDate;
        private String employeeProfileId;
        private String resolution;
    }

    public Item getItem(Integer index){
        return lists.get(index);
    }

}
