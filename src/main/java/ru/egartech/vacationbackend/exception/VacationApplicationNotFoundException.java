package ru.egartech.vacationbackend.exception;

public class VacationApplicationNotFoundException extends RuntimeException{

    public VacationApplicationNotFoundException(String message) {
        super(message);
    }
}
