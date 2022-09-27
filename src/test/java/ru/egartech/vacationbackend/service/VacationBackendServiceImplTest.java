package ru.egartech.vacationbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.mock.mockito.MockBean;
import ru.egartech.vacationbackend.AbstractSpringContext;
import ru.egartech.vacationbackend.manager.VacationManager;

import ru.egartech.vacationbackend.model.VacationDto;
import ru.egartech.vacationbackend.model.VacationApprovalReqDto;
import ru.egartech.vacationbackend.util.VacationTestTemplate;
import ru.egartech.vacationbackend.util.DateMills;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacationBackendServiceImplTest extends AbstractSpringContext {

    @MockBean
    private VacationManager vacationManager;

    private VacationDto validVacation;

    @Autowired
    private ru.egartech.vacationbackend.service.VacationsBackendService vacationsBackendService;

    @BeforeEach
    void setMockOutput() {
        validVacation = VacationTestTemplate.getValid();
        when(vacationManager.findVacationsByEgarId("username",180311895)).thenReturn(List.of(validVacation));
    }

    @DisplayName("Test find vacation by id")
    @Test
    void findVacationById() {
        when(vacationManager.getVacationById("2wmaha")).thenReturn(Optional.of(validVacation));
        VacationDto t = vacationsBackendService.findVacationById("2wmaha");
        assertEquals(validVacation, t);
    }

    @Test
    void getVacation() {
        when(vacationManager.getVacationsByListId(List.of("2wmaha"))).thenReturn(List.of(validVacation));
        List<VacationDto> t = vacationsBackendService.getVacation(List.of("2wmaha"));
        assertNotNull(t);
        assertNotNull(t.get(0));
        assertEquals(validVacation, t.get(0));
    }

    @Test
    void checkCalculationOfVacationDates() {
        //Валидный отпуск который должен прийти в VacationRepository из сервиса
        var vacationFromService = VacationDto.builder()
                .employeeProfileId("2wrahmn")
                .startDate(DateMills.of("08-08-2022 00:00:00")) //Sun Aug 07 2022 21:00:00 1659906000000L
                .endDate(DateMills.of("21-08-2022 23:59:59"))
                .build();
        validVacation.setStartDate(DateMills.of("08-08-2022 00:00:00"));
        validVacation.setEndDate(DateMills.of("21-08-2022 23:59:59"));
        when(vacationManager.saveVacation(vacationFromService, 180311895)).thenReturn(validVacation);
        var vacationApprovalReq = VacationApprovalReqDto.builder()
                .startDate(DateMills.of("08-08-2022 00:00:00"))
                .employeeProfileId("2wrahmn")
                .countDay(14)
                .listProfileId(180311895)
                .build();
        VacationDto expected = vacationsBackendService.addVacationRequest(vacationApprovalReq);
        assertEquals(vacationApprovalReq.getStartDate(), expected.getStartDate());
        assertEquals(DateMills.of("21-08-2022 23:59:59"), expected.getEndDate());
    }

    @Test
    void getRemainVacationDaysByEgarId() {
        var vacationFirst = VacationTestTemplate.getValid();
        var vacationSecond = VacationTestTemplate.getValid();
        //первый отпуск 14 дней
        vacationFirst.setStartDate(DateMills.of("01-03-2022 00:00:00"));
        vacationFirst.setEndDate(DateMills.of("14-03-2022 23:59:59"));
        //второй отпуск 8 дней
        vacationSecond.setStartDate(DateMills.of("01-08-2022 00:00:00"));
        vacationSecond.setEndDate(DateMills.of("08-08-2022 23:59:59"));
        when(vacationManager.findVacationsByEgarIdByStatus(any(), anyInt(), any()))
                .thenReturn(List.of(vacationFirst, vacationSecond));
        //дата начала работы год назад
        Long jobStartDate = LocalDateTime.now().minusYears(1L).atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        var expectVacationDays = vacationsBackendService.getRemainVacationDaysByEgarId("username",
                jobStartDate,
                180311895);
        //14 + 8 = 22 дня отгуляно, и 28 - 22 = 6 осталось
        assertEquals(6, expectVacationDays.getVacationDayRemain());
    }

    @Test
    void updateVacationById() {
        when(vacationManager.updateVacation(validVacation.getVacationId(), validVacation)).thenReturn(validVacation);
        var expected = vacationsBackendService.updateVacationById(validVacation.getVacationId(),validVacation);
        assertEquals(validVacation, expected);
    }

    @Test
    void getVacationsByEgarIdWithoutEndDateAndStartDate() {
        when(vacationManager.findVacationsByEgarId("username", 180311895)).thenReturn(List.of(validVacation));
        List<VacationDto> t = vacationsBackendService.getVacationsByEgarId("username", 180311895, null, null);
        assertNotNull(t);
        assertNotNull(t.get(0));
        assertEquals(validVacation, t.get(0));
    }

    @Test
    void getVacationsByEgarIdWithEndDateAndStartDate() {
        when(vacationManager.findVacationsByEgarId("username", 180311895)).thenReturn(List.of(validVacation));
        List<VacationDto> t = vacationsBackendService.getVacationsByEgarId(
                "username",
                180311895,
                DateMills.of("07-08-2022 23:59:00"),
                DateMills.of("08-08-2022 00:00:01")
        );
        assertNotNull(t);
        assertNotNull(t.get(0));
        assertEquals(validVacation, t.get(0));
    }

    @Test
    void getVacationsByEgarIdWithoutTimeInterval(){
        var vacationBeforeTimeInterval = VacationTestTemplate.getValid();
        var vacationAfterTimeInterval = VacationTestTemplate.getValid();
        vacationBeforeTimeInterval.setStartDate(DateMills.of("07-08-2022 23:59:59"));
        vacationAfterTimeInterval.setStartDate(DateMills.of("08-08-2022 00:00:02"));
        when(vacationManager.findVacationsByEgarId("username", 180311895))
                .thenReturn(List.of(validVacation, vacationBeforeTimeInterval, vacationAfterTimeInterval));
        List<VacationDto> t = vacationsBackendService.getVacationsByEgarId(
                "username",
                180311895,
                DateMills.of("08-08-2022 00:00:00"),
                DateMills.of("08-08-2022 00:00:01")
        );
        assertNotNull(t);
        assertEquals(1, t.size());
        assertNotNull(t.get(0));
        assertEquals(validVacation, t.get(0));
    }

    @Test
    void getRemainVacationDays() {
        var vacationFirst = VacationTestTemplate.getValid();
        var vacationSecond = VacationTestTemplate.getValid();
        //первый отпуск 14 дней
        vacationFirst.setStartDate(DateMills.of("01-03-2022 00:00:00"));
        vacationFirst.setEndDate(DateMills.of("14-03-2022 23:59:59"));
        //второй отпуск 8 дней
        vacationSecond.setStartDate(DateMills.of("01-08-2022 00:00:00"));
        vacationSecond.setEndDate(DateMills.of("08-08-2022 23:59:59"));

        when(vacationManager.findVacationsByListIdByStatus(anyList(), any()))
                .thenReturn(List.of(vacationFirst, vacationSecond));
        //дата начала работы год назад
        Long jobStartDate = LocalDateTime.now().minusYears(1L).atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        var expectVacationDays = vacationsBackendService.getRemainVacationDays(
                jobStartDate, List.of("2cdrtm", "2ghfjd"));
        //14 + 8 = 22 дня отгуляно, и 28 - 22 = 6 осталось
        assertEquals(6, expectVacationDays.getVacationDayRemain());
    }
}