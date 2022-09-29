package ru.egartech.vacationbackend.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.egartech.vacationbackend.exception.VacationNotFoundException;
import ru.egartech.vacationbackend.model.VacationApprovalReqDto;
import ru.egartech.vacationbackend.model.VacationDayRemainDto;
import ru.egartech.vacationbackend.model.VacationDto;
import ru.egartech.vacationbackend.manager.VacationManager;
import ru.egartech.vacationbackend.service.VacationsService;
import ru.egartech.vacationbackend.util.DateUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.time.temporal.TemporalAdjusters.*;

@Service
@RequiredArgsConstructor
public class VacationServiceImpl implements VacationsService {

    private final Integer VACATION_DAYS_IN_YEAR = 28;
    private final VacationManager vacationManager;

    @Override
    public VacationDto addVacationRequest(VacationApprovalReqDto vacationApprovalReqDto) {
        return vacationManager.saveVacation(VacationDto.builder()
                        .employeeProfileId(vacationApprovalReqDto.getEmployeeProfileId())
                        .startDate(vacationApprovalReqDto.getStartDate())
                        .endDate(calculateEndDate(vacationApprovalReqDto.getStartDate(), vacationApprovalReqDto.getCountDay()))
                        .build(),
                vacationApprovalReqDto.getListProfileId());
    }

    @Override
    public VacationDto findVacationById(String vacationId) {
        return vacationManager.getVacationById(vacationId).orElseThrow(() -> new VacationNotFoundException(
                String.format("Не удалось найти отпуск с ID: %s", vacationId)));
    }

    @Override
    public VacationDayRemainDto getRemainVacationDays(Long jobStartDate, List<String> vacationIds) {
        List<VacationDto> lvOff = vacationManager.findVacationsByListIdByStatus(vacationIds, VacationDto.StatusTypeEnum.DONE);
        Integer dayRemain = calculateDayRemain(lvOff, jobStartDate);
        return VacationDayRemainDto.builder()
                .vacationDayRemain(dayRemain)
                .build();
    }

    @Override
    public VacationDayRemainDto getRemainVacationDaysByEgarId(String egarId, Long jobStartDate, Integer profileListId) {
        List<VacationDto> lvOff = vacationManager.findVacationsByEgarIdByStatus(egarId, profileListId, VacationDto.StatusTypeEnum.DONE);
        Integer dayRemain = calculateDayRemain(lvOff, jobStartDate);
        return VacationDayRemainDto.builder()
                .vacationDayRemain(dayRemain)
                .build();
    }

    @Override
    public List<VacationDto> getVacation(List<String> vacationIds) {
        return vacationManager.getVacationsByListId(vacationIds);
    }

    @Override
    public List<VacationDto> getVacationsByEgarId(String egarId, Integer profileListId, Long startDate, Long endDate) {
        return vacationManager.findVacationsByEgarId(egarId, profileListId).stream()
                .filter(checkVacationDate(startDate, endDate))
                .collect(Collectors.toList());
    }

    @Override
    public VacationDto updateVacationById(String vacationId, VacationDto vacationDto) {
        return vacationManager.updateVacation(vacationId, vacationDto);
    }

    private Predicate<VacationDto> checkVacationDate(Long startDate, Long endDate){
        return v -> {
            if(startDate != null & endDate != null){
                return DateUtils.toLocalDate(v.getStartDate()).isAfter(DateUtils.toLocalDate(startDate)) &
                        DateUtils.toLocalDate(v.getStartDate()).isBefore(DateUtils.toLocalDate(endDate));
            }else {
                LocalDate firstDayYear = LocalDate.now().with(firstDayOfYear());
                LocalDate lastDayYear = LocalDate.now().with(firstDayOfNextYear());
                return DateUtils.toLocalDate(v.getStartDate()).isAfter(firstDayYear) &
                        DateUtils.toLocalDate(v.getStartDate()).isBefore(lastDayYear);
            }
        };
    }

    private Integer calculateDayRemain(List<VacationDto> vacationDone, Long jobStartDate){
        long countVacationDayOff = vacationDone.stream().mapToLong(d ->
                TimeUnit.MILLISECONDS.toDays(d.getEndDate() - d.getStartDate())+ 1L).sum() ;
        LocalDateTime startJob = LocalDateTime.ofInstant(Instant.ofEpochMilli(jobStartDate), ZoneId.systemDefault());
        long experienceInMonths = ChronoUnit.MONTHS.between(startJob, LocalDateTime.now());
        return (BigDecimal.valueOf(VACATION_DAYS_IN_YEAR)
                .multiply(BigDecimal.valueOf(experienceInMonths))
                .divide(BigDecimal.valueOf(12), RoundingMode.CEILING))
                .subtract(BigDecimal.valueOf(countVacationDayOff))
                .intValue();
    }

    private Long calculateEndDate(Long startDate, Integer countDay){

        return Long.sum(startDate, TimeUnit.DAYS.toMillis(countDay)) - TimeUnit.SECONDS.toMillis(1L);
    }

}
