package ru.egartech.vacationbackend.manager.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.egartech.sdk.api.TaskClient;
import ru.egartech.sdk.dto.task.deserialization.TaskDto;
import ru.egartech.sdk.dto.task.deserialization.customfield.field.relationship.RelationshipFieldDto;
import ru.egartech.sdk.dto.task.deserialization.customfield.field.relationship.RelationshipValueDto;
import ru.egartech.sdk.dto.task.serialization.CreateTaskDto;
import ru.egartech.sdk.dto.task.serialization.UpdateTaskDto;
import ru.egartech.sdk.dto.task.serialization.customfield.request.CustomFieldRequest;
import ru.egartech.sdk.dto.task.serialization.customfield.update.BindFieldDto;

import ru.egartech.sdk.exception.clickup.ClickUpException;
import ru.egartech.sdk.exception.task.TaskNotFoundException;
import ru.egartech.vacationbackend.exception.VacationNotFoundException;
import ru.egartech.vacationbackend.exception.EmployeeNotFoundException;
import ru.egartech.vacationbackend.property.MessageProperties;
import ru.egartech.vacationbackend.property.ProfileProperty;
import ru.egartech.vacationbackend.mapper.VacationMapper;
import ru.egartech.vacationbackend.property.VacationProperty;
import ru.egartech.vacationbackend.model.VacationDto;
import ru.egartech.vacationbackend.manager.VacationManager;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class VacationManagerImpl implements VacationManager {

    private final TaskClient taskClient;
    private final VacationMapper vacationMapper;
    private final VacationProperty vacationProperty;
    private final ProfileProperty profileProperty;
    private final MessageProperties messageProperties;

    @Override
    public Optional<VacationDto> getVacationById(String vacationId) {
        return findTaskById(vacationId).map(vacationMapper::toVacation);
    }

    @Override
    public List<VacationDto> getVacationsByListId(List<String> vacationIdList) {
        return vacationIdList.stream()
                .map(this::findVacationById)
                .collect(Collectors.toList());
    }

    @Override
    public VacationDto saveVacation(VacationDto vacationDto, Integer profileListId) {
        TaskDto employeeProfile = findTaskById(vacationDto.getEmployeeProfileId())
                .orElseThrow(() -> new EmployeeNotFoundException(
                        messageProperties.getEmployeeNotFoundMessage(vacationDto.getEmployeeProfileId())));
        var createTaskDto = CreateTaskDto.builder()
                .name(employeeProfile.getName().replace("Сотрудник", "Отпуск"))
                .build();
        int vacationListId = profileProperty.getItem(profileListId).getVacationList();
        TaskDto newTask = taskClient.createTask(vacationListId, createTaskDto);
        var updateTaskDto = UpdateTaskDto.builder()
                .id(newTask.getId())
                .customFields(getBindField(vacationDto, vacationListId))
                .customField(BindFieldDto.linkTask(vacationProperty.getItem(vacationListId).getEmployeeProfileId(), employeeProfile.getId()))
                .build();
        newTask = taskClient.updateTask(updateTaskDto);
        return vacationMapper.toVacation(newTask);
    }

    @Override
    public VacationDto updateVacation(String vacationId, VacationDto vacationDto) {
        var vacationTask = findTaskById(vacationId).orElseThrow(() ->
                new VacationNotFoundException(messageProperties.getVacationNotFoundMessage(vacationId)));
        var updateTaskDto = UpdateTaskDto.builder()
                .id(vacationId)
                .customFields(getBindField(vacationDto, vacationTask.getList().getId()))
                .build();
        var updatedTask = taskClient.updateTask(updateTaskDto);
        return vacationMapper.toVacation(updatedTask);
    }

    @Override
    public List<VacationDto> findVacationsByEgarId(String egarId, Integer listId) {
        return getListVacationIds(egarId, listId).stream()
                .map(this::findVacationById)
                .collect(Collectors.toList());
    }

    @Override
    public List<VacationDto> findVacationsByListIdByStatus(List<String> vacationIdList, VacationDto.StatusTypeEnum status) {
        return vacationIdList.stream()
                .map(this::findVacationById)
                .filter(t -> t.getStatusType().equals(status))
                .collect(Collectors.toList());
    }

    @Override
    public List<VacationDto> findVacationsByEgarIdByStatus(String egarId, Integer profileListId, VacationDto.StatusTypeEnum status) {
        return getListVacationIds(egarId, profileListId).stream()
                .map(this::findVacationById)
                .filter(t -> t.getStatusType().equals(status))
                .collect(Collectors.toList());
    }

    private List<BindFieldDto> getBindField(VacationDto vacationDto, Integer vacationListId) {
        return List.of(BindFieldDto.of(vacationProperty.getItem(vacationListId).getStartDate(), vacationDto.getStartDate()),
                BindFieldDto.of(vacationProperty.getItem(vacationListId).getEndDate(), vacationDto.getEndDate()));
    }

    private List<String> getListVacationIds(String egarId, Integer profileListId) {
        TaskDto employee;
        try {
            var customField = CustomFieldRequest.builder()
                    .fieldId(profileProperty.getItem(profileListId).getEgarId())
                    .value(egarId).build();
            employee = taskClient.getTasksByCustomFields(profileListId, false, customField).getFirstTask();
        } catch (TaskNotFoundException e) {
            throw new EmployeeNotFoundException(messageProperties.getEmployeeNotFoundMessage(egarId));
        }
        var vacationsField = employee.<RelationshipFieldDto>customField(profileProperty.getItem(profileListId).getVacationListId());
        var vacationsFieldValue = vacationsField.getValue();
        return vacationsFieldValue.stream().map(RelationshipValueDto::getId).toList();
    }

    private Optional<TaskDto> findTaskById(String id) {
        try {
            return Optional.of(taskClient.getTaskById(id, true));
        } catch (ClickUpException e) {
            return Optional.empty();
        }
    }

    private VacationDto findVacationById(String vacationId) {
        return findTaskById(vacationId).map(vacationMapper::toVacation)
                .orElseThrow(() ->
                        new VacationNotFoundException(messageProperties.getVacationNotFoundMessage(vacationId)));
    }

}
