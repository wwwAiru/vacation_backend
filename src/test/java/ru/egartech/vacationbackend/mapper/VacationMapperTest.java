package ru.egartech.vacationbackend.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.ResourceUtils;
import ru.egartech.sdk.api.TaskClient;
import ru.egartech.sdk.dto.task.deserialization.TaskDto;
import ru.egartech.sdk.dto.task.deserialization.TasksDto;
import ru.egartech.vacationbackend.AbstractSpringContext;
import ru.egartech.vacationbackend.model.VacationDto;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VacationMapperTest extends AbstractSpringContext {

    @MockBean
    TaskClient taskClient;

    @Autowired
    VacationMapper vacationMapper;
    @Autowired
    ObjectMapper objectMapper;
    TaskDto vacationTask;
    TasksDto assignersTask;

    @BeforeEach
    void init() throws IOException {
        File f1 =  ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX.concat("task.json"));
        File f2 =  ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX.concat("assigner.json"));
        vacationTask = objectMapper.readValue(f1, TaskDto.class);
        assignersTask = objectMapper.readValue(f2, TasksDto.class);
    }

    @Test
    void toVacation() {
        VacationDto expect = VacationDto.builder()
                .vacationId("2wmahab")
                .employeeProfileId("2rgq20y")
                .assigners(List.of(ru.egartech.vacationbackend.model.AssignerDto.builder()
                        .orgStructureId("2vj3zk9")
                        .fullName("Фамилия Имя Отчество")
                        .build()))
                .startDate(1667350800000L)
                .endDate(1668474000000L)
                .status("согласован")
                .statusType(VacationDto.StatusTypeEnum.DONE)
                .statusId("subcat180311910_sc156545942_pdD65bGE")
                .build();
        when(taskClient.getTasksByCustomFields(anyInt(), anyBoolean(), any())).thenReturn(assignersTask);
        VacationDto actual = vacationMapper.toVacation(vacationTask);
        assertNotNull(actual);
        assertEquals(actual, expect);
    }
}