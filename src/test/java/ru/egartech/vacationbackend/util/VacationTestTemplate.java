package ru.egartech.vacationbackend.util;

import lombok.Getter;
import ru.egartech.vacationbackend.model.VacationDto;

import java.util.List;


@Getter
public class VacationTestTemplate {

    public static VacationDto getValid(){
        return VacationDto.builder()
                .vacationId("2wmahab")
                .employeeProfileId("2wrahmn")
                .status("новый")
                .startDate(DateMills.of("08-08-2022 00:00:00")) //Sun Aug 07 2022 21:00:00 1659906000000L
                .endDate(DateMills.of("21-08-2022 23:59:59"))
                .assigners(List.of(ru.egartech.vacationbackend.model.AssignerDto.builder()
                        .fullName("Иванов Иван Иванович")
                        .avatarUrl("https://clickup.ru/avatar.png")
                        .orgStructureId("3rtrfmk")
                        .build()))
                .statusId("subcat180311910_sc156545942_pdD65bGE")
                .statusType(VacationDto.StatusTypeEnum.DONE)
                .build();
    }

    public static VacationDto getEmpty(){
        return VacationDto.builder().build();
    }

}
