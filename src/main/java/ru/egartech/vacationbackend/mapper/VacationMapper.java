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
import ru.egartech.vacationbackend.exception.EmployeeNotFoundException;
import ru.egartech.vacationbackend.property.MessageProperties;
import ru.egartech.vacationbackend.property.VacationProperty;
import ru.egartech.vacationbackend.model.VacationDto;
import ru.egartech.vacationbackend.model.AssignerDto;

import java.util.List;


@Component
@RequiredArgsConstructor
public class VacationMapper {

    private final VacationProperty vacationProperty;
    private final MessageProperties messageProperties;
    private final TaskClient taskClient;

    @Value("${org_structure.list_id}")
    private Integer orgStructureListId;
    @Value("${org_structure.cu_egar_id}")
    private String cuEgarId;
    @Value("${org_structure.firstname}")
    private String firstnameId;
    @Value("${org_structure.lastname}")
    private String lastnameId;
    @Value("${org_structure.patronymic}")
    private String patronymicId;
    @Value("${org_structure.avatar}")
    private String avatarId;

    public VacationDto toVacation(TaskDto taskDto){
        Integer listId = taskDto.getList().getId();
        TextFieldDto startDateField = taskDto.customField(vacationProperty.getItem(listId).getStartDate());
        TextFieldDto endDateField = taskDto.customField(vacationProperty.getItem(listId).getEndDate());
        String startDate = startDateField.getValue();
        String endDate = endDateField.getValue();
        RelationshipFieldDto employeeRelationShip = taskDto.customField(vacationProperty.getItem(listId).getEmployeeProfileId());
        String employeeProfileId = employeeRelationShip.getValue().stream()
                .findFirst()
                .map(RelationshipValueDto::getId).orElseThrow(() -> new EmployeeNotFoundException(
                        messageProperties.getVacationNotFoundMessage(taskDto.getId())));
        List<String> assignersCuEgarId = taskDto.getAssigners().stream()
                .map(ru.egartech.sdk.dto.task.deserialization.customfield.assigner.AssignerDto::getId)
                .toList();
        List<AssignerDto> assigners = assignersCuEgarId.stream()
                    .map(this::mapAssigner)
                    .toList();
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
            var customFieldRequest = CustomFieldRequest.builder()
                    .fieldId(this.cuEgarId)
                    .value(cuEgarId)
                    .build();
            assignerTask = taskClient.getTasksByCustomFields(orgStructureListId, true, customFieldRequest)
                    .getFirstTask();
        } catch (TaskNotFoundException ex){
            throw new AssignerNotFoundException(messageProperties.getAssignerNotFoundMessage(cuEgarId));
        }
        TextFieldDto lastName = assignerTask.customField(lastnameId);
        TextFieldDto firstName = assignerTask.customField(lastnameId);
        TextFieldDto patronymic = assignerTask.customField(lastnameId);
        AttachmentFieldDto avatarUrlField = assignerTask.customField(avatarId);
        String avatarUrl = avatarUrlField.getValue().stream()
                .findFirst()
                .map(AttachmentDto::getUrl)
                .orElse(null);
        return AssignerDto.builder()
                .orgStructureId(assignerTask.getId())
                .fullName(String.format("%s %s %s", lastName.getValue(), firstName.getValue(), patronymic.getValue()))
                .avatarUrl(avatarUrl)
                .build();
    }

}
