package ru.egartech.vacationbackend.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.egartech.sdk.api.ListTaskClient;
import ru.egartech.sdk.api.TaskClient;
import ru.egartech.sdk.dto.task.deserialization.TaskDto;
import ru.egartech.sdk.dto.task.serialization.CreateTaskDto;
import ru.egartech.sdk.dto.task.serialization.RequestTaskDto;
import ru.egartech.sdk.dto.task.serialization.UpdateTaskDto;
import ru.egartech.sdk.dto.task.serialization.customfield.update.BindFieldDto;
import ru.egartech.vacationbackend.mapper.VacationMapper;
import ru.egartech.vacationbackend.config.ClickUpListIdConfiguration;
import ru.egartech.vacationbackend.model.VacationDto;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class VacationRepositoryImpl implements VacationRepository{

    private final TaskClient taskClient;
    private final VacationMapper vacationMapper;
    private final Integer LIST_ID = 180311910;
    private final ListTaskClient listTaskClient;

    private final ClickUpListIdConfiguration cf;

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

    private List<BindFieldDto> getBindField(VacationDto vacationDto){
        return List.of(BindFieldDto.of(cf.getLists().get(LIST_ID).get("start_date"), vacationDto.getStartDate()),
                       BindFieldDto.of(cf.getLists().get(LIST_ID).get("end_date"), vacationDto.getEndDate()));
    }

}
