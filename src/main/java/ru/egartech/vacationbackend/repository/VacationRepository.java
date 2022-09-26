package ru.egartech.vacationbackend.repository;

import ru.egartech.vacationbackend.model.VacationDto;

import java.util.List;
import java.util.Optional;


public interface VacationRepository {

    Optional<VacationDto> getVacationById(String vacationId);

    List<VacationDto> getVacationsByListId(List<String> vacationIdList);

    VacationDto saveVacation(VacationDto vacationDto, Integer profileListId);

    VacationDto updateVacation(String vacationId, VacationDto vacationDto);

    List<VacationDto> findVacationsByEgarId(String egarId, Integer listId);

    List<VacationDto> findVacationsByListIdByStatus(List<String> vacationIdList, VacationDto.StatusTypeEnum status);

    List<VacationDto> findVacationsByEgarIdByStatus(String egarId, Integer profileListId, VacationDto.StatusTypeEnum status);
}
