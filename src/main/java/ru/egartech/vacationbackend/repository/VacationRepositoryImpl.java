package ru.egartech.vacationbackend.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.egartech.sdk.api.TaskClient;
import ru.egartech.sdk.dto.task.deserialization.TaskDto;
import ru.egartech.sdk.dto.task.deserialization.customfield.field.relationship.RelationshipFieldDto;
import ru.egartech.sdk.dto.task.deserialization.customfield.field.relationship.RelationshipValueDto;
import ru.egartech.sdk.dto.task.serialization.CreateTaskDto;
import ru.egartech.sdk.dto.task.serialization.RequestTaskDto;
import ru.egartech.sdk.dto.task.serialization.UpdateTaskDto;
import ru.egartech.sdk.dto.task.serialization.customfield.request.CustomFieldRequest;
import ru.egartech.sdk.dto.task.serialization.customfield.update.BindFieldDto;

import ru.egartech.vacationbackend.configure.ProfileClickUpListIdConfiguration;
import ru.egartech.vacationbackend.mapper.VacationMapper;
import ru.egartech.vacationbackend.configure.VacationClickUpListIdConfiguration;
import ru.egartech.vacationbackend.model.VacationDto;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class VacationRepositoryImpl implements VacationRepository {

    private final TaskClient taskClient;
    private final VacationMapper vacationMapper;
    private final VacationClickUpListIdConfiguration cf;
    private final ProfileClickUpListIdConfiguration pcf;

    @Override
    public VacationDto getVacationById(String vacationId) {
        TaskDto taskVacation = taskClient.getTaskById(vacationId, true);
        return vacationMapper.toVacation(taskVacation);
    }

    @Override
    public List<VacationDto> getVacationsByListId(List<String> vacationIdList) {
        return vacationIdList.stream().map(v -> vacationMapper.toVacation(taskClient.getTaskById(v, true)))
                .collect(Collectors.toList());
    }

    @Override
    public VacationDto saveVacation(VacationDto vacationDto, Integer profileListId) {
        TaskDto employeeProfile = taskClient.getTaskById(vacationDto.getEmployeeProfileId(), true);
        String name = employeeProfile.getName().replace("Сотрудник:", "Отпуск:");
        RequestTaskDto createTaskDto = CreateTaskDto.builder()
                .name(name)
                .build();
        int vacationListId = Integer.parseInt(pcf.getLists().get(profileListId).get("vacation_list"));
        TaskDto newTaskDto = taskClient.createTask(vacationListId, createTaskDto);
        UpdateTaskDto updateTaskDto = UpdateTaskDto.builder()
                .id(newTaskDto.getId())
                .customFields(getBindField(vacationDto, vacationListId))
                .customField(BindFieldDto.linkTask(cf.getLists().get(vacationListId).get("employee_profile_id"), employeeProfile.getId()))
                .build();
        TaskDto updateTask = taskClient.updateTask(updateTaskDto);
        return vacationMapper.toVacation(updateTask);
    }

    @Override
    public VacationDto updateVacation(String vacationId, VacationDto vacationDto) {
        TaskDto updateTask = taskClient.getTaskById(vacationId, true);
        UpdateTaskDto updateTaskDto = UpdateTaskDto.builder()
                .id(vacationId)
                .customFields(getBindField(vacationDto, updateTask.getList().getId()))
                .build();
        TaskDto updatedTask = taskClient.updateTask(updateTaskDto);
        return vacationMapper.toVacation(updatedTask);
    }

    @Override
    public List<VacationDto> findVacationByEgarId(String egarId, Integer listId) {
        return getListVacationIds(egarId, listId).stream()
                .map(v -> vacationMapper.toVacation(taskClient.getTaskById(v, true)))
                .collect(Collectors.toList());
    }

    @Override
    public List<VacationDto> findVacationByListIdByStatus(List<String> vacationIdList, String status) {
        return vacationIdList.stream()
                .map(t -> taskClient.getTaskById(t, true))
                .filter(t -> t.getStatus().getType().equals(status))
                .map(vacationMapper::toVacation)
                .collect(Collectors.toList());
    }

    @Override
    public List<VacationDto> findVacationByEgarIdByStatus(String egarId, Integer profileListId, String status) {
        return getListVacationIds(egarId, profileListId).stream()
                .map(t -> taskClient.getTaskById(t, true))
                .filter(t -> t.getStatus().getType().equals(status))
                .map(vacationMapper::toVacation)
                .collect(Collectors.toList());
    }

    private List<BindFieldDto> getBindField(VacationDto vacationDto, Integer vacationListId) {
        return List.of(BindFieldDto.of(cf.getLists().get(vacationListId).get("start_date"), vacationDto.getStartDate()),
                BindFieldDto.of(cf.getLists().get(vacationListId).get("end_date"), vacationDto.getEndDate()));
    }

    private List<String> getListVacationIds(String egarId, Integer profileListId) {
        TaskDto e = taskClient.getTasksByCustomFields(profileListId, false, CustomFieldRequest.builder()
                .fieldId(pcf.getLists().get(profileListId).get("egar_id"))
                .operator("=")
                .value(egarId).build()).getFirstTask();
        RelationshipFieldDto vacationsField = e.customField(pcf.getLists().get(profileListId).get("vacation_list_id"));
        List<RelationshipValueDto> vac = vacationsField.getValue();
        return vac.stream().map(RelationshipValueDto::getId).toList();
    }

}
