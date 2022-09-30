package ru.egartech.vacationbackend.exception;

import ru.egartech.sdk.exception.clickup.ClickUpException;

public class VacationApplicationNotFoundException extends ClickUpException {

    public VacationApplicationNotFoundException(String message) {
        super(message);
    }
}
