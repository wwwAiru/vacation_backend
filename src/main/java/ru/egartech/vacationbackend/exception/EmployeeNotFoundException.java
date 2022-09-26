package ru.egartech.vacationbackend.exception;

public class EmployeeNotFoundException extends VacationApplicationNotFoundException{
    public EmployeeNotFoundException(String message) {
        super(message);
    }
}
