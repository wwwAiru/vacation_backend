package ru.egartech.vacationbackend.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Data
@ConfigurationProperties(prefix = "set.vacation")
public class VacationProperty {
    private Map<Integer, Item> lists;
    @Data
    public static class Item{
        private String startDate;
        private String endDate;
        private String employeeProfileId;
    }

    public Item getItem(Integer tr){
        return lists.get(tr);
    }

}
