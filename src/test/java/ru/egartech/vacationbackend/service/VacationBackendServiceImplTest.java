package ru.egartech.vacationbackend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.mock.mockito.MockBean;
import ru.egartech.vacationbackend.AbstractSpringContext;
import ru.egartech.vacationbackend.repository.VacationRepository;

import ru.egartech.vacationbackend.service.VacationsBackendService;

import ru.egartech.vacationbackend.model.VacationDto;
import ru.egartech.vacationbackend.model.AssignerDto;
import ru.egartech.vacationbackend.model.VacationApprovalReqDto;
import ru.egartech.vacationbackend.util.VacationTestTemplate;
import ru.egartech.vacationbackend.util.dateMills;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacationBackendServiceImplTest extends AbstractSpringContext {

    @MockBean
    private VacationRepository vacationRepository;

    private VacationDto validVacation;

    @Autowired
    private VacationsBackendService vacationsBackendService;

    @BeforeEach
    void setMockOutput() {
        validVacation = VacationTestTemplate.getValid();

        when(vacationRepository.findVacationByEgarId("username",180311895)).thenReturn(List.of(validVacation));
    }

    @DisplayName("Test find vacation by id")
    @Test
    void findVacationById() {
        when(vacationRepository.getVacationById("2wmaha")).thenReturn(validVacation);
        VacationDto t = vacationsBackendService.findVacationById("2wmaha");
        assertEquals(validVacation, t);
    }

    @Test
    void getVacation() {
        when(vacationRepository.getVacationsByListId(List.of("2wmaha"))).thenReturn(List.of(validVacation));
        List<VacationDto> t = vacationsBackendService.getVacation(List.of("2wmaha"));
        assertNotNull(t);
        assertNotNull(t.get(0));
        assertEquals(validVacation, t.get(0));
    }

    @Test
    void checkCalculationOfVacationDates() {
        var vacationFromService = VacationDto.builder()
                .employeeProfileId("2wrahmn")
                .startDate(dateMills.of("08-08-2022 00:00:00")) //Sun Aug 07 2022 21:00:00 1659906000000L
                .endDate(dateMills.of("21-08-2022 23:59:59"))
                .build();
        validVacation.setStartDate(dateMills.of("08-08-2022 00:00:00"));
        when(vacationRepository.saveVacation(vacationFromService, 180311895)).thenReturn(validVacation);
        VacationApprovalReqDto v = VacationApprovalReqDto.builder()
                .startDate(dateMills.of("08-08-2022 00:00:00"))
                .employeeProfileId("2wrahmn")
                .countDay(14)
                .listProfileId(180311895)
                .build();
        VacationDto expected = vacationsBackendService.addVacationRequest(v);
        assertEquals(v.getStartDate(), expected.getStartDate());
        assertEquals(dateMills.of("21-08-2022 23:59:59"), expected.getEndDate());
    }

    @Test
    void getRemainVacationDays() {
        when(vacationRepository.findVacationByEgarIdByStatus(any(), anyInt(), any())).thenReturn(List.of(validVacation));

//        vacationsBackendService.getRemainVacationDays(LocalDateTime.of(2022, 8, 23,))

    }

    @Test
    void updateVacationById() {

    }

    @Test
    void getVacationsByEgarIdWithoutEndDateAndStartDate() {
        when(vacationRepository.findVacationByEgarId("username", 180311895)).thenReturn(List.of(validVacation));
        List<VacationDto> t = vacationsBackendService.getVacationsByEgarId("username", 180311895, null, null);
        assertNotNull(t);
        assertNotNull(t.get(0));
        assertEquals(validVacation, t.get(0));
    }

    @Test
    void getVacationsByEgarIdWithEndDateAndStartDate() {
        VacationDto expectVacation = VacationDto.builder()
                .vacationId("2wmahab")
                .employeeProfileId("2wrahmn")
                .status("новый")
                .startDate(dateMills.of("08-08-2022 00:00:00")) //Sun Aug 07 2022 21:00:00 1659906000000L
                .endDate(dateMills.of("21-08-2022 23:59:59"))
                .assigners(List.of(AssignerDto.builder()
                        .fullName("Иванов Иван Иванович")
                        .avatarUrl("https://clickup.ru/avatar.png")
                        .orgStructureId("3rtrfmk")
                        .build()))
                .statusId("subcat180311910_sc156545942_pdD65bGE")
                .statusType(VacationDto.StatusTypeEnum.DONE)
                .build();

        when(vacationRepository.findVacationByEgarId("username", 180311895)).thenReturn(List.of(expectVacation));
        List<VacationDto> t = vacationsBackendService.getVacationsByEgarId(
                "username",
                180311895,
                dateMills.of("07-08-2022 23:59:00"),
                dateMills.of("08-08-2022 00:00:01")
        );
        assertNotNull(t);
        assertNotNull(t.get(0));
        assertEquals(expectVacation, t.get(0));
    }

    @Test
    void getVacationsByEgarIdWithEndDateAndStartDate2(){

    }
}