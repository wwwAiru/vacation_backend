package ru.egartech.vacationbackend.property;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageProperties {

    private final MessageSource messageSource;

    private static final String VACATION_NOT_FOUND_KEY = "vacation_not_found";
    private static final String EMPLOYEE_NOT_FOUND_KEY = "employee_not_found";
    private static final String ASSIGNER_NOT_FOUND_KEY = "assigner_not_found";

    public String getVacationNotFoundMessage(String id) {
        return messageSource.getMessage(VACATION_NOT_FOUND_KEY, new Object[] { id }, LocaleContextHolder.getLocale());
    }

    public String getEmployeeNotFoundMessage(String id) {
        return messageSource.getMessage(EMPLOYEE_NOT_FOUND_KEY, new Object[] { id }, LocaleContextHolder.getLocale());
    }

    public String getAssignerNotFoundMessage(String id) {
        return messageSource.getMessage(ASSIGNER_NOT_FOUND_KEY, new Object[] {id},LocaleContextHolder.getLocale());
    }
}
