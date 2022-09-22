package ru.egartech.vacationbackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.egartech.sdk.api.TaskClient;
import ru.egartech.sdk.dto.task.deserialization.TaskDto;
import ru.egartech.vacationbackend.EgarIdNullExceptions;
import ru.egartech.vacationbackend.model.VacationApprovalReqDto;
import ru.egartech.vacationbackend.model.VacationDayRemainDto;
import ru.egartech.vacationbackend.model.VacationDto;
import ru.egartech.vacationbackend.repository.VacationRepository;
import ru.egartech.vacationbackend.service.VacationsBackendService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;

@Service
@RequiredArgsConstructor
public class VacationBackendServiceImpl implements VacationsBackendService {

    private final Integer VACATION_DAYS_IN_YEAR = 28;
    private final VacationRepository vacationRepository;

    @Override
    public VacationDto addVacationRequest(VacationApprovalReqDto vacationApprovalReqDto) {
        Long endDate = Long.sum(vacationApprovalReqDto.getStartDate(),
                TimeUnit.DAYS.toMillis(vacationApprovalReqDto.getCountDay()));
        return vacationRepository.saveVacation(VacationDto.builder()
                .employeeProfileId(vacationApprovalReqDto.getEmployeeProfileId())
                .startDate(vacationApprovalReqDto.getStartDate())
                .endDate(endDate)
                .build(), vacationApprovalReqDto.getListProfileId());
    }

    @Override
    public VacationDto findVacationById(String vacationId, String listId) {
        return vacationRepository.getVacationById(vacationId);
    }

    @Override
    public VacationDayRemainDto getRemainVacationDays(Long jobStartDate, Integer listId, List<String> vacationId,
                                                      String egarId) {
        if(egarId != null){
            List<VacationDto> lvOff = vacationRepository.findVacationByEgarIdByStatus(egarId, listId, "done");
            Integer dayRemain = calculateDayRemain(lvOff, jobStartDate);
            return VacationDayRemainDto.builder()
                    .vacationDayRemain(dayRemain)
                    .build();
        }
        else if (!vacationId.isEmpty()){
            List<VacationDto> lvOff = vacationRepository.findVacationByListIdByStatus(vacationId, "done");
            Integer dayRemain = calculateDayRemain(lvOff, jobStartDate);
            return VacationDayRemainDto.builder()
                    .vacationDayRemain(dayRemain)
                    .build();
        }
        else throw new EgarIdNullExceptions("Egar id not be Null");
    }

    @Override
    public List<VacationDto> getVacation(List<String> vacationIds, String egarId, Long startDate, Long endDate, Integer profileListId) {
        if(egarId != null){
            if(startDate != null & endDate != null){
                return vacationRepository.findVacationByEgarId(egarId, profileListId).stream()
                        .filter(isIncludesTimeInterval(startDate, endDate))
                        .collect(Collectors.toList());
            }
            else {
                startDate = LocalDateTime.now().with(firstDayOfYear())
                        .atZone(ZoneId.systemDefault())
                        .toInstant().toEpochMilli();
                endDate = LocalDateTime.now().with(lastDayOfYear())
                        .atZone(ZoneId.systemDefault())
                        .toInstant().toEpochMilli();
                return vacationRepository.findVacationByEgarId(egarId, profileListId)
                        .stream()
                        .filter(isIncludesTimeInterval(startDate, endDate))
                        .collect(Collectors.toList());
            }
        }
        else if(!vacationIds.isEmpty()){
            return vacationRepository.getVacationsByListId(vacationIds);
        }
        else throw new EgarIdNullExceptions("Egar id not be Null");
    }

    @Override
    public VacationDto updateVacationById(String vacationId, VacationDto vacationDto) {
        return vacationRepository.updateVacation(vacationId, vacationDto);
    }

    private Predicate<VacationDto> isIncludesTimeInterval(Long startDate, Long endDate){
        return v -> v.getStartDate().compareTo(startDate) >= 0 & v.getStartDate().compareTo(endDate) <= 0;
    }

    private Integer calculateDayRemain(List<VacationDto> vacationDone, Long jobStartDate){
        long countVacationDayOff = vacationDone.stream().mapToLong(d ->
                TimeUnit.MILLISECONDS.toDays(d.getEndDate() - d.getStartDate())).sum() + 1L;
        LocalDateTime startJob = LocalDateTime.ofInstant(Instant.ofEpochMilli(jobStartDate), ZoneId.systemDefault());
        long experienceInMonths = ChronoUnit.MONTHS.between(startJob, LocalDateTime.now());
        BigDecimal md = (BigDecimal.valueOf(VACATION_DAYS_IN_YEAR).divide(BigDecimal.valueOf(12L), RoundingMode.CEILING))
                .multiply(BigDecimal.valueOf(experienceInMonths));
        Long dayRemain = md.subtract(BigDecimal.valueOf(countVacationDayOff)).longValue();
        return dayRemain.intValue();
    }

}
