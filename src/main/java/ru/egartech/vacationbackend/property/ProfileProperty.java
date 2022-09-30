package ru.egartech.vacationbackend.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Data
@ConfigurationProperties(prefix = "settings.profile")
public class ProfileProperty {
    private Map<Integer, Item> lists;

    @Data
    public static class Item {
        private String egarId;
        private String vacationListId;
        private Integer vacationList;
    }

    public Item getItem(Integer tr){
        return lists.get(tr);
    }
}
