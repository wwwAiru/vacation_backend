package ru.egartech.vacationbackend.repository;

import org.springframework.stereotype.Repository;
import ru.egartech.vacationbackend.model.VacationDto;

import java.util.List;


public interface VacationRepository {

    VacationDto getVacationById(String vacationId);

    List<VacationDto> getVacationsByListId(List<String> vacationIdList);

    VacationDto saveVacation(VacationDto vacationDto);

    VacationDto updateVacation(String vacationId, VacationDto vacationDto);
}
