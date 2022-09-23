package ru.egartech.vacationbackend.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.egartech.sdk.api.TaskClient;
import ru.egartech.sdk.dto.task.deserialization.TaskDto;

import ru.egartech.vacationbackend.AbstractSpringContext;
import ru.egartech.vacationbackend.configure.VacationClickUpListIdConfiguration;
import ru.egartech.vacationbackend.mapper.VacationMapper;
import ru.egartech.vacationbackend.repository.VacationRepository;
import ru.egartech.vacationbackend.repository.VacationRepositoryImpl;
import ru.egartech.vacationbackend.service.VacationsBackendService;

import ru.egartech.vacationbackend.model.VacationDto;
import ru.egartech.vacationbackend.model.AssignerDto;
import ru.egartech.vacationbackend.model.VacationApprovalReqDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacationBackendServiceImplTest extends AbstractSpringContext {

    @MockBean
    private VacationRepository vacationRepository;

    private VacationDto vacationDto;

    @Autowired
    private VacationsBackendService vacationsBackendService;

    @BeforeEach
    void setMockOutput() {
        vacationDto = VacationDto.builder()
                .vacationId("2wmahab")
                .employeeProfileId("2wrahmn")
                .status("новый")
                .startDate(1659906000000L)
                .endDate(166800199999L)
                .assigners(List.of(AssignerDto.builder()
                        .fullName("Иванов Иван Иванович")
                        .avatarUrl("https://ava.ru")
                        .orgStructureId("3rtrfmk")
                        .build()))
                .build();
        when(vacationRepository.getVacationById("2wmaha")).thenReturn(vacationDto);
        when(vacationRepository.findVacationByEgarId("username",180311895)).thenReturn(List.of(vacationDto));
    }


    @DisplayName("Test find vacation by id")
    @Test
    void findVacationById() {
        VacationDto t = vacationsBackendService.findVacationById("2wmaha");
        assertEquals(vacationDto, t);
    }

    @Test
    void getVacation() {
        List<VacationDto> t = vacationsBackendService.getVacation(List.of("2wmaha"));

        assertNotNull(t);
        assertNotNull(t.get(0));
        assertEquals(vacationDto, t.get(0));
    }

    @Test
    void addVacationRequest() {
        when(vacationRepository.saveVacation(any(VacationDto.class), anyInt())).thenReturn(vacationDto);

        VacationApprovalReqDto v = VacationApprovalReqDto.builder()
                .startDate(165590599999L)
                .employeeProfileId("2wmaha")
                .countDay(14)
                .listProfileId(180311895)
                .build();

        long l2 = Long.sum(TimeUnit.DAYS.toMillis(14), 165590599999L);
        assertEquals(vacationsBackendService.addVacationRequest(v), vacationDto);
        assertEquals(vacationsBackendService.addVacationRequest(v).getEndDate(), l2);
    }

    @Test
    void getRemainVacationDays() {
        when(vacationRepository.findVacationByEgarIdByStatus(any(), anyInt(), any())).thenReturn(List.of(vacationDto));

//        vacationsBackendService.getRemainVacationDays(LocalDateTime.of(2022, 8, 23,))

    }

    @Test
    void updateVacationById() {
    }
}