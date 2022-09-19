package ru.egartech.vacationbackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.egartech.sdk.api.TaskClient;
import ru.egartech.vacationbackend.model.VacationApprovalReqDto;
import ru.egartech.vacationbackend.model.VacationDayRemainDto;
import ru.egartech.vacationbackend.model.VacationDto;
import ru.egartech.vacationbackend.repository.VacationRepository;
import ru.egartech.vacationbackend.service.VacationsBackendService;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VacationBackendServiceImpl implements VacationsBackendService {

    private final String EGAR_ID_CUSTOM_FIELD_ID = "836c9684-0c71-4714-aff2-900b0ded0685";
    private final String VACATION_CUSTOM_FIELD_ID = "856d3fd6-b982-4085-aa67-65d60bf41bb7";
    private final String START_DATE_CUSTOM_FIELD_ID = "58004edd-d04c-4edb-bed8-f6337397fd72";
    private final String END_DATE_CUSTOM_FIELD_ID = "879e67e3-eb65-45eb-9a82-94ac94470918";
    private final String DEVS_LIST_ID = "180311895";


    private final TaskClient taskClient;
    private final VacationRepository vacationRepository;



    @Override
    public VacationDto addVacationRequest(VacationApprovalReqDto vacationApprovalReqDto) {

        Long endDate = Long.sum(Long.parseLong(vacationApprovalReqDto.getStartDate()),
                TimeUnit.DAYS.toMillis(vacationApprovalReqDto.getCountDay()));
        return vacationRepository.saveVacation(VacationDto.builder()
                .employeeProfileId("2rgq20y")
                .startDate(vacationApprovalReqDto.getStartDate())
                .endDate(Long.toString(endDate))
                .build());
    }

    @Override
    public VacationDto findVacationById(String vacationId, String listId) {
        return vacationRepository.getVacationById(vacationId);
    }

    @Override
    public VacationDayRemainDto getRemainVacationDays(String jobStartDate, List<String> vacationId, String egarId, String startDate, String endDate, String listId) {
        return null;
    }

    @Override
    public List<VacationDto> getVacationUser(List<String> vacationId, String egarId, String startDate, String endDate, String listId) {
        return vacationRepository.getVacationsByListId(vacationId);
    }

    @Override
    public VacationDto updateVacationById(String vacationId, VacationDto vacationDto) {
        return null;
    }
}
