package ru.egartech.vacationbackend.mapper;

import lombok.RequiredArgsConstructor;
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


import java.util.List;


@Component
@RequiredArgsConstructor
public class VacationMapper {

    private final VacationClickUpListIdConfiguration lc;
    private final TaskClient taskClient;

    @Value("${org_structure.list_id}")
    private Integer ORG_STRUCTURE_LIST_ID;
    @Value("${org_structure.cu_egar_id}")
    private String CU_EGAR_ID;
    @Value("${org_structure.firstname}")
    private String FIRSTNAME_ID;
    @Value("${org_structure.lastname}")
    private String LASTNAME_ID;
    @Value("${org_structure.patronymic}")
    private String PATRONYMIC_ID;
    @Value("${org_structure.avatar}")
    private String AVATAR_ID;
    @Value("${org_structure.root_task_org_struct}")
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
                e.<TextFieldDto>customField(PATRONYMIC_ID).getValue());
        String avatarUrl = e.<AttachmentFieldDto>customField(AVATAR_ID).getValue().stream()
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
