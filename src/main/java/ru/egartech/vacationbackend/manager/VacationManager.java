package ru.egartech.vacationbackend.manager;

import ru.egartech.vacationbackend.model.VacationDto;

import java.util.List;
import java.util.Optional;


public interface VacationManager {

    Optional<VacationDto> getVacationById(String vacationId);

    List<VacationDto> getVacationsByListId(List<String> vacationIdList);

    VacationDto saveVacation(VacationDto vacationDto, Integer profileListId);

    VacationDto updateVacation(String vacationId, VacationDto vacationDto);

    List<VacationDto> findVacationsByEgarId(String egarId, Integer listId);

    List<VacationDto> findVacationsByListIdByStatus(List<String> vacationIdList, VacationDto.StatusTypeEnum status);

    List<VacationDto> findVacationsByEgarIdByStatus(String egarId, Integer profileListId, VacationDto.StatusTypeEnum status);
}
