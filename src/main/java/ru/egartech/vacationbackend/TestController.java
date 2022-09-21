package ru.egartech.vacationbackend;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.egartech.vacationbackend.repository.VacationRepository;

@RestController
@RequestMapping("test")
@RequiredArgsConstructor
public class TestController {
    private final VacationRepository vacationRepository;

    @GetMapping("{vacationId}")
    public String test(@PathVariable String vacationId){
        return vacationRepository.getVacationById(vacationId).toString();
    }
}
