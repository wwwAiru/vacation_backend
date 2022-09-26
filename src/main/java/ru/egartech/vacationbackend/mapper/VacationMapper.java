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
import ru.egartech.sdk.exception.task.TaskNotFoundException;
import ru.egartech.vacationbackend.exception.AssignerNotFoundException;
import ru.egartech.vacationbackend.property.VacationProperty;
import ru.egartech.vacationbackend.model.VacationDto;
import ru.egartech.vacationbackend.model.AssignerDto;


import java.util.List;


@Component
@RequiredArgsConstructor
public class VacationMapper {

    private static final String EMPLOYEE_PROFILE_ID = "employee_profile_id";
    private static final String START_DATE = "start_date";
    private static final String END_DATE = "end_date";
    private final VacationProperty lc;
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
        String startDate = taskDto.<TextFieldDto>customField(lc.getLists().get(listId).get(START_DATE)).getValue();
        String endDate = taskDto.<TextFieldDto>customField(lc.getLists().get(listId).get(END_DATE)).getValue();
        String employeeProfileId = taskDto.<RelationshipFieldDto>customField(
                lc.getLists()
                        .get(listId)
                        .get(EMPLOYEE_PROFILE_ID))
                .getValue().stream()
                .findFirst()
                .map(RelationshipValueDto::getId).orElse(null);
        List<String> assignersCuEgarId = taskDto.getAssigners().stream()
                .map(a -> a.getId())
                .toList();
        List<AssignerDto> assigners = null;
        if (!assignersCuEgarId.isEmpty()){
            assigners = assignersCuEgarId.stream()
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

    private AssignerDto mapAssigner(String cuEgarId) {
        TaskDto assignerTask;
        try{
            assignerTask = taskClient.getTasksByCustomFields(ORG_STRUCTURE_LIST_ID,
                            true, CustomFieldRequest.builder()
                                    .fieldId(CU_EGAR_ID)
                                    .operator("=")
                                    .value(cuEgarId)
                                    .build())
                    .getFirstTask();
        } catch (TaskNotFoundException ex){
            throw new AssignerNotFoundException(String.format("Не удалось найти согласующего с CU EGAR ID: %s", cuEgarId));
        }
        String fullName = String.format("%s %s %s", assignerTask.<TextFieldDto>customField(LASTNAME_ID).getValue(),
                assignerTask.<TextFieldDto>customField(FIRSTNAME_ID).getValue(),
                assignerTask.<TextFieldDto>customField(PATRONYMIC_ID).getValue());
        String avatarUrl = assignerTask.<AttachmentFieldDto>customField(AVATAR_ID).getValue().stream()
                .findFirst()
                .map(AttachmentDto::getUrl)
                .orElse(null);
        return AssignerDto.builder()
                .orgStructureId(assignerTask.getId())
                .fullName(fullName)
                .avatarUrl(avatarUrl)
                .build();
    }

}
