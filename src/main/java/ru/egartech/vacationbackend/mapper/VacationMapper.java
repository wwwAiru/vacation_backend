package ru.egartech.vacationbackend.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.egartech.sdk.dto.task.deserialization.TaskDto;
import ru.egartech.sdk.dto.task.deserialization.customfield.field.relationship.RelationshipFieldDto;
import ru.egartech.sdk.dto.task.deserialization.customfield.field.relationship.RelationshipValueDto;
import ru.egartech.sdk.dto.task.deserialization.customfield.field.text.TextFieldDto;
import ru.egartech.vacationbackend.config.ClickUpListIdConfiguration;
import ru.egartech.vacationbackend.model.VacationDto;
import ru.egartech.vacationbackend.model.AssignerDto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VacationMapper {

    private final ClickUpListIdConfiguration lc;

    public VacationDto toVacation(TaskDto taskDto){
        Integer listId = taskDto.getList().getId();
        String startDate = taskDto.<TextFieldDto>customField(lc.getLists().get(listId).get("start_date")).getValue();
        String endDate = taskDto.<TextFieldDto>customField(lc.getLists().get(listId).get("end_date")).getValue();
        String employeeProfileId = taskDto.<RelationshipFieldDto>customField(
                lc.getLists()
                        .get(listId)
                        .get("employee_profile_id"))
                .getValue().stream()
                .findFirst()
                .map(RelationshipValueDto::getId).orElse(null);

        List<AssignerDto> la = taskDto.getAssigners().stream()
                .map((a) -> AssignerDto.builder()
                        .username(a.getUsername())
                        .egarId(trimEgarIdFromEmail(a.getEmail()))
                        .build())
                .collect(Collectors.toList());
        return VacationDto.builder()
                .vacationId(taskDto.getId())
                .employeeProfileId(employeeProfileId)
                .startDate(startDate)
                .endDate(endDate)
                .assigners(la)
                .status(taskDto.getStatus().getStatus())
                .build();
    }

    private String trimEgarIdFromEmail(String email){
        return email.substring(0,email.indexOf('@'));
    }
}


//"status": {
//        "id": "subcat180311910_sc156545942_rIcopcWR",
//        "status": "новый",
//        "color": "#d3d3d3",
//        "orderindex": 0,
//        "type": "open"
//        },

// "status": {
//         "id": "subcat180311910_sc156545942_w2XJMHgo",
//         "status": "в процессе",
//         "color": "#f9d900",
//         "orderindex": 6,
//         "type": "custom"
//         },

//"status": {
//        "id": "subcat180311910_sc156545942_pdD65bGE",
//        "status": "согласован",
//        "color": "#04A9F4",
//        "orderindex": 5,
//        "type": "custom"
//        },

//"status": {
//        "id": "subcat180311910_sc156545942_PpKdymx7",
//        "status": "завершен",
//        "color": "#6bc950",
//        "orderindex": 7,
//        "type": "closed"
//        },

//"status": {
//        "id": "subcat180311910_sc156545942_8Gqmlkpf",
//        "status": "согл-ть у куратора",
//        "color": "#0231E8",
//        "orderindex": 1,
//        "type": "custom"
//        },

//"status": {
//        "id": "subcat180311910_sc156545942_e3G5twb4",
//        "status": "оформление в кадрах",
//        "color": "#0231E8",
//        "orderindex": 4,
//        "type": "custom"
//        },