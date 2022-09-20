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
import ru.egartech.vacationbackend.config.ProfileClickUpListIdConfiguration;
import ru.egartech.vacationbackend.mapper.VacationMapper;
import ru.egartech.vacationbackend.config.VacationClickUpListIdConfiguration;
import ru.egartech.vacationbackend.model.VacationDto;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class VacationRepositoryImpl implements VacationRepository{

    private final TaskClient taskClient;
    private final VacationMapper vacationMapper;
    private final Integer LIST_ID = 180311910;

    private final String list_egar_org_struct = "836c9684-0c71-4714-aff2-900b0ded0685";
    private final String _egar_id_profile = "836c9684-0c71-4714-aff2-900b0ded0685";
    private final String cu_user_id = "3ccda2f7-5228-4ce1-b6f7-3f37a3056330";

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
    public VacationDto saveVacation(VacationDto vacationDto) {
        TaskDto employeeProfile = taskClient.getTaskById(vacationDto.getEmployeeProfileId(), true);
        String name = employeeProfile.getName().replace("Сотрудник:", "Отпуск:");

        RequestTaskDto createTaskDto = CreateTaskDto.ofName(name);

        TaskDto newTaskDto = taskClient.createTask(LIST_ID, createTaskDto);

        UpdateTaskDto updateTaskDto = UpdateTaskDto
                .ofTaskId(newTaskDto.getId())
                .linkTask(cf.getLists().get(LIST_ID).get("employee_profile_id"), employeeProfile.getId())
                .bindCustomFields(getBindField(vacationDto));

        TaskDto updateTask = taskClient.updateTask(updateTaskDto);

        return vacationMapper.toVacation(updateTask);
    }

    @Override
    public VacationDto updateVacation(String vacationId, VacationDto vacationDto) {
        UpdateTaskDto updateTaskDto = UpdateTaskDto
                .ofTaskId(vacationId)
                .bindCustomFields(getBindField(vacationDto));
        TaskDto updateTask = taskClient.updateTask(updateTaskDto);
        return vacationMapper.toVacation(updateTask);
    }

    @Override
    public List<VacationDto> findVacationByEgarId(String egarId, Integer listId) {

        TaskDto e = taskClient.getTasksByCustomFields(listId, CustomFieldRequest.create()
                .setFieldId(pcf.getLists().get(listId).get("egar_id"))
                .setOperator("=")
                .setValue(egarId)).getFirstTask();

        RelationshipFieldDto vacationsField = e.customField(pcf.getLists().get(listId).get("vacation_list_id"));
        List<RelationshipValueDto> vac = vacationsField.getValue();

        List<String> ids = vac.stream().map(RelationshipValueDto::getId).toList();

        return ids.stream()
                .map(v -> vacationMapper.toVacation(taskClient.getTaskById(v, true)))
                .collect(Collectors.toList());
    }

    private List<BindFieldDto> getBindField(VacationDto vacationDto){
        return List.of(BindFieldDto.of(cf.getLists().get(LIST_ID).get("start_date"), vacationDto.getStartDate()),
                       BindFieldDto.of(cf.getLists().get(LIST_ID).get("end_date"), vacationDto.getEndDate()));
    }

}
