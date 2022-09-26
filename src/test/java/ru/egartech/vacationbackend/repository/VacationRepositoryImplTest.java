package ru.egartech.vacationbackend.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.ResourceUtils;
import ru.egartech.sdk.api.TaskClient;
import ru.egartech.sdk.dto.task.deserialization.TaskDto;
import ru.egartech.sdk.dto.task.deserialization.TasksDto;
import ru.egartech.vacationbackend.AbstractSpringContext;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import ru.egartech.vacationbackend.model.VacationDto;
import ru.egartech.vacationbackend.util.DateMills;

@ExtendWith(MockitoExtension.class)
class VacationRepositoryImplTest extends AbstractSpringContext {

    @MockBean
    TaskClient taskClient;

    @Autowired
    VacationRepository vacationRepository;

    @Autowired
    ObjectMapper objectMapper;

    TaskDto vacationTask;
    TaskDto profileTask;
    TasksDto assignersTasks;
    TasksDto profilesTasks;

    @BeforeEach
    void init() throws IOException {
        File f =  ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX
                    .concat("task.json"));
        File f2 = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX.concat("assigner.json"));
        File profileFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX.concat("profile.json"));
        File profilesFile = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX.concat("profiles.json"));
        vacationTask = objectMapper.readValue(f, TaskDto.class);
        assignersTasks = objectMapper.readValue(f2, TasksDto.class);
        profileTask = objectMapper.readValue(profileFile, TaskDto.class);
        profilesTasks = objectMapper.readValue(profilesFile, TasksDto.class);
    }

    @Test
    void getVacationById() {
        when(taskClient.getTaskById(any(), anyBoolean())).thenReturn(vacationTask);
        when(taskClient.getTasksByCustomFields(anyInt(), anyBoolean(), any())).thenReturn(assignersTasks);
        var v = vacationRepository.getVacationById("2wmahab");
        assertTrue(v.isPresent());
    }

    @Test
    void getVacationsByListId() {
        when(taskClient.getTaskById(any(), anyBoolean())).thenReturn(vacationTask);
        when(taskClient.getTasksByCustomFields(anyInt(), anyBoolean(), any())).thenReturn(assignersTasks);
        List<VacationDto> v = vacationRepository.getVacationsByListId(List.of("2wmahab"));
        assertNotNull(v);
        assertEquals(1, v.size());
    }

    @Test
    void saveVacation() {
        when(taskClient.createTask(anyInt(), any())).thenReturn(vacationTask);
        when(taskClient.getTaskById(any(),anyBoolean())).thenReturn(profileTask);
        when(taskClient.updateTask(any())).thenReturn(vacationTask);
        when(taskClient.getTasksByCustomFields(anyInt(), anyBoolean(), any())).thenReturn(assignersTasks);
        var vacation = VacationDto.builder()
                .vacationId("2wmahab")
                .employeeProfileId("2rgq20y")
                .startDate(DateMills.of("01-01-2022 00:00:00"))
                .endDate(DateMills.of("14-01-2022 23:59:59"))
                .build();
        VacationDto expect = vacationRepository.saveVacation(vacation, 180311895);
        assertNotNull(expect);
    }

    @Test
    void updateVacation() {
        when(taskClient.getTaskById(any(),anyBoolean())).thenReturn(profileTask);
        when(taskClient.updateTask(any())).thenReturn(vacationTask);
        var vacation = VacationDto.builder()
                .vacationId("2wmahab")
                .employeeProfileId("2rgq20y")
                .startDate(DateMills.of("01-01-2022 00:00:00"))
                .endDate(DateMills.of("14-01-2022 23:59:59"))
                .build();
        VacationDto expect = vacationRepository.updateVacation("2wmahab", vacation);
        assertNotNull(expect);
    }

    @Test
    void findVacationByEgarId() {
        when(taskClient.getTaskById(any(),anyBoolean())).thenReturn(vacationTask);
        when(taskClient.getTasksByCustomFields(anyInt(), anyBoolean(), any())).thenReturn(profilesTasks);
        List<VacationDto> expect = vacationRepository.findVacationsByEgarId("username", 180311895);
        assertNotNull(expect);
        assertEquals(1, expect.size());
        assertNotNull(expect.get(0));
    }
}