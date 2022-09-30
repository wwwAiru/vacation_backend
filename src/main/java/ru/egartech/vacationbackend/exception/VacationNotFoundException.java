package ru.egartech.vacationbackend.exception;

public class VacationNotFoundException extends VacationApplicationNotFoundException{
    public VacationNotFoundException(String message) {
        super(message);
    }
}
