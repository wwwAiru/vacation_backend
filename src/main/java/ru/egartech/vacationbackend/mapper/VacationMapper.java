package ru.egartech.vacationbackend.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.egartech.sdk.api.TaskClient;
import ru.egartech.sdk.dto.task.deserialization.TaskDto;

import ru.egartech.sdk.dto.task.deserialization.customfield.field.attachment.AttachmentDto;
import ru.egartech.sdk.dto.task.deserialization.customfield.field.attachment.AttachmentFieldDto;
import ru.egartech.sdk.dto.task.deserialization.customfield.field.relationship.RelationshipFieldDto;
import ru.egartech.sdk.dto.task.deserialization.customfield.field.relationship.RelationshipValueDto;
import ru.egartech.sdk.dto.task.deserialization.customfield.field.text.TextFieldDto;
import ru.egartech.sdk.dto.task.serialization.customfield.request.CustomFieldRequest;
import ru.egartech.vacationbackend.configure.VacationClickUpListIdConfiguration;
import ru.egartech.vacationbackend.model.VacationDto;
import ru.egartech.vacationbackend.model.AssignerDto;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class VacationMapper {

    private final VacationClickUpListIdConfiguration lc;
    private final TaskClient taskClient;

    @Value("${org_structure_list_id}")
    private Integer ORG_STRUCTURE_LIST_ID;
    @Value("${cu_egar_id}")
    private String CU_EGAR_ID;
    @Value("${firstname}")
    private String FIRSTNAME_ID;
    @Value("${lastname}")
    private String LASTNAME_ID;
    @Value("${otc}")
    private String OTC_ID;
    @Value("${ava}")
    private String AVA_ID;
    @Value("${root_task_org_struct}")
    private String ROOT_TASK_ORG_STRUCT;

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
        List<String> assignerCuEgarId = taskDto.getAssigners().stream()
                .map(a -> a.getId())
                .toList();
        List<AssignerDto> assigners = null;
        if (!assignerCuEgarId.isEmpty()){
            assigners = assignerCuEgarId.stream()
                    .map(this::mapAssigner)
                    .toList();
        }

        return VacationDto.builder()
                .vacationId(taskDto.getId())
                .employeeProfileId(employeeProfileId)
                .startDate(Long.valueOf(startDate))
                .endDate(Long.valueOf(endDate))
                .assigners(assigners)
                .status(taskDto.getStatus().getStatus())
                .statusType(VacationDto.StatusTypeEnum.fromValue(taskDto.getStatus().getType()))
                .statusId(taskDto.getStatus().getId())
                .build();
    }

    private String trimEgarIdFromEmail(String email){
        return email.substring(0,email.indexOf('@'));
    }

    private AssignerDto mapAssigner(String a) {
        TaskDto e = taskClient.getTasksByCustomFields(ORG_STRUCTURE_LIST_ID,
                true, CustomFieldRequest.builder()
                        .fieldId(CU_EGAR_ID)
                        .operator("=")
                        .value(a).build()).getFirstTask();
        String fullName = String.format("%s %s %s", e.<TextFieldDto>customField(FIRSTNAME_ID).getValue(),
                e.<TextFieldDto>customField(LASTNAME_ID).getValue(),
                e.<TextFieldDto>customField(LASTNAME_ID).getValue());
        String avatarUrl = e.<AttachmentFieldDto>customField(AVA_ID).getValue().stream()
                .findFirst()
                .map(AttachmentDto::getUrl)
                .orElse(null);
        return AssignerDto.builder()
                .orgStructureId(e.getId())
                .fullName(fullName)
                .avatarUrl(avatarUrl)
                .build();
    };

}
