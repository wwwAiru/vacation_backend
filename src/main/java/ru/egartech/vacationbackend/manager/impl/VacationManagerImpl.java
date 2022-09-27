package ru.egartech.vacationbackend.manager.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import ru.egartech.sdk.api.TaskClient;
import ru.egartech.sdk.dto.task.deserialization.TaskDto;
import ru.egartech.sdk.dto.task.deserialization.customfield.field.relationship.RelationshipFieldDto;
import ru.egartech.sdk.dto.task.deserialization.customfield.field.relationship.RelationshipValueDto;
import ru.egartech.sdk.dto.task.serialization.CreateTaskDto;
import ru.egartech.sdk.dto.task.serialization.UpdateTaskDto;
import ru.egartech.sdk.dto.task.serialization.customfield.request.CustomFieldRequest;
import ru.egartech.sdk.dto.task.serialization.customfield.update.BindFieldDto;

import ru.egartech.sdk.exception.task.TaskNotFoundException;
import ru.egartech.vacationbackend.exception.VacationNotFoundException;
import ru.egartech.vacationbackend.exception.EmployeeNotFoundException;
import ru.egartech.vacationbackend.exception.VacationSaveException;
import ru.egartech.vacationbackend.exception.VacationUpdateException;
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

    private static final String VACATION_LIST = "vacation_list";
    private static final String EMPLOYEE_PROFILE_ID = "employee_profile_id";
    private static final String START_DATE = "start_date";
    private static final String END_DATE = "end_date";
    private static final String VACATION_LIST_ID = "vacation_list_id";
    private static final String EGAR_ID = "egar_id";
    private final TaskClient taskClient;
    private final VacationMapper vacationMapper;
    private final VacationProperty cf;
    private final ProfileProperty pcf;

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
        //получение карточки сотрудника для привязки отпуска
        TaskDto employeeProfile = findTaskById(vacationDto.getEmployeeProfileId())
                .orElseThrow(() ->
                        new EmployeeNotFoundException(
                                String.format("Не удалось найти сотрудника с ID: %s", vacationDto.getEmployeeProfileId())));
        //создание новой таски
        var createTaskDto = CreateTaskDto.builder()
                .name(employeeProfile.getName().replace("Сотрудник", "Отпуск"))
                .build();
        TaskDto newTask;
        int vacationListId;
        try {
            vacationListId = Integer.parseInt(pcf.getLists().get(profileListId).get(VACATION_LIST));
            newTask = taskClient.createTask(vacationListId, createTaskDto);
        } catch (RuntimeException e) {
            throw new VacationSaveException(e);
        }
        //обновление таски
        try {
            var updateTaskDto = UpdateTaskDto.builder()
                    .id(newTask.getId())
                    .customFields(getBindField(vacationDto, vacationListId))
                    .customField(BindFieldDto.linkTask(cf.getLists().get(vacationListId).get(EMPLOYEE_PROFILE_ID), employeeProfile.getId()))
                    .build();
            newTask = taskClient.updateTask(updateTaskDto);
        } catch (RuntimeException e) {
            throw new VacationUpdateException(e);
        }
        return vacationMapper.toVacation(newTask);
    }

    @Override
    public VacationDto updateVacation(String vacationId, VacationDto vacationDto) {
        //проверка отпуска на существование
        var vacationTask = findTaskById(vacationId).orElseThrow(() ->
                new VacationNotFoundException(
                        String.format("Не удалось найти отпуск с ID: %s", vacationId)));
        try {
            var updateTaskDto = UpdateTaskDto.builder()
                    .id(vacationId)
                    .customFields(getBindField(vacationDto, vacationTask.getList().getId()))
                    .build();
            var updatedTask = taskClient.updateTask(updateTaskDto);
            return vacationMapper.toVacation(updatedTask);
        } catch (RuntimeException e) {
            throw new VacationUpdateException(e);
        }
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
        return List.of(BindFieldDto.of(cf.getLists().get(vacationListId).get(START_DATE), vacationDto.getStartDate()),
                BindFieldDto.of(cf.getLists().get(vacationListId).get(END_DATE), vacationDto.getEndDate()));
    }

    private List<String> getListVacationIds(String egarId, Integer profileListId) {
        TaskDto employee;
        try {
            employee = taskClient.getTasksByCustomFields(profileListId, false, CustomFieldRequest.builder()
                    .fieldId(pcf.getLists().get(profileListId).get(EGAR_ID))
                    .operator("=")
                    .value(egarId).build()).getFirstTask();
        } catch (TaskNotFoundException e){
            throw new EmployeeNotFoundException(
                    String.format("Не удалось найти сотрудника с EGAR ID: %s", egarId));
        }
        var vacationsField = employee.<RelationshipFieldDto>customField(pcf.getLists().get(profileListId).get(VACATION_LIST_ID));
        var vacationsFieldValue = vacationsField.getValue();
        return vacationsFieldValue.stream().map(RelationshipValueDto::getId).toList();
    }

    private Optional<TaskDto> findTaskById(String id) {
        try {
            return Optional.of(taskClient.getTaskById(id, true));
        } catch (HttpClientErrorException e) {
            return Optional.empty();
        }
    }

    private VacationDto findVacationById(String vacationId){
        return findTaskById(vacationId).map(vacationMapper::toVacation)
                .orElseThrow(() ->
                        new VacationNotFoundException(
                                String.format("Не удалось найти отпуск с ID: %s", vacationId)));
    }

}
