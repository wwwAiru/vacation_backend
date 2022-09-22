package ru.egartech.vacationbackend.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.egartech.sdk.api.TaskClient;
import ru.egartech.sdk.dto.task.deserialization.TaskDto;

import ru.egartech.vacationbackend.configure.VacationClickUpListIdConfiguration;
import ru.egartech.vacationbackend.mapper.VacationMapper;
import ru.egartech.vacationbackend.repository.VacationRepository;
import ru.egartech.vacationbackend.repository.VacationRepositoryImpl;
import ru.egartech.vacationbackend.service.VacationsBackendService;

import ru.egartech.vacationbackend.model.VacationDto;
import ru.egartech.vacationbackend.model.AssignerDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@SpringBootTest
class VacationBackendServiceImplTest {

    VacationDto vacationDto = VacationDto.builder()
            .vacationId("2wmahab")
            .employeeProfileId("2wrahmn")
            .status("новый")
            .startDate(1659906000000L)
            .endDate(1660424400000L)
            .assigners(List.of(AssignerDto.builder()
                    .fullName("Иванов Иван Иванович")
                    .avatarUrl("https://ava.ru")
                    .orgStructureId("3rtrfmk")
                    .build()))
            .build();

    @Test
    void findVacationById() {
        VacationRepository vacationRepository = mock(VacationRepositoryImpl.class);
        doReturn(vacationDto).when(vacationRepository).getVacationById("2wmaha");
        VacationsBackendService vacationsBackendService = new VacationBackendServiceImpl(vacationRepository);
        VacationDto t = vacationsBackendService.findVacationById("2wmaha", "105005");
        assertEquals(vacationDto, t);
    }

    @Test
    void getVacation() {
        VacationRepository vacationRepository = mock(VacationRepositoryImpl.class);
        doReturn(List.of(vacationDto)).when(vacationRepository).findVacationByEgarId("username",180311895);
        VacationsBackendService vacationsBackendService = new VacationBackendServiceImpl(vacationRepository);
        List<VacationDto> t = vacationsBackendService.getVacation(null, "username",
                null, null, 180311895);

        assertNotNull(t);
        assertNotNull(t.get(0));

        List<VacationDto> t2 = vacationsBackendService.getVacation(null, "username", 165990599999L,
                165990599998L,180311895);
        assertTrue(t2.isEmpty());
    }

    @Test
    void addVacationRequest() {


    }

    @Test
    void getRemainVacationDays() {
    }

    @Test
    void updateVacationById() {
    }
}