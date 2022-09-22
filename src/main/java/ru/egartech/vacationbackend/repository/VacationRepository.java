package ru.egartech.vacationbackend.repository;

import org.springframework.stereotype.Repository;
import ru.egartech.vacationbackend.model.VacationDto;

import java.util.List;


public interface VacationRepository {

    VacationDto getVacationById(String vacationId);

    List<VacationDto> getVacationsByListId(List<String> vacationIdList);

    VacationDto saveVacation(VacationDto vacationDto, Integer profileListId);

    VacationDto updateVacation(String vacationId, VacationDto vacationDto);

    List<VacationDto> findVacationByEgarId(String egarId, Integer listId);

    List<VacationDto> findVacationByListIdByStatus(List<String> vacationIdList, String status);

    List<VacationDto> findVacationByEgarIdByStatus(String egarId, Integer profileListId, String status);
}
