package ru.egartech.vacationbackend.controlleradvice;

//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.*;
//import ru.egartech.vacationbackend.exception.VacationNotFoundException;
//import ru.egartech.vacationbackend.exception.VacationApplicationNotFoundException;
//import ru.egartech.vacationbackend.exception.VacationNotFoundException;
//import ru.egartech.vacationbackend.model.ViolationDto;
//import ru.egartech.vacationbackend.model.ErrorResponseDto;
//
//import javax.validation.ConstraintViolationException;
//import java.time.LocalDateTime;
//import java.time.ZoneOffset;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

//@RestControllerAdvice
public class ErrorHandlingControllerAdvice {

//    @ResponseBody
//    @ExceptionHandler(ConstraintViolationException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public List<ViolationDto> onConstraintValidationException(ConstraintViolationException e) {
//        return e.getConstraintViolations().stream()
//                .map(violation -> ViolationDto.builder()
//                                .fieldName(violation.getPropertyPath().toString())
//                                .message(violation.getMessage())
//                                .build())
//                .collect(Collectors.toList());
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    @ResponseBody
//    public List<ViolationDto> onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
//        return e.getBindingResult().getFieldErrors().stream()
//                .map(error -> new ViolationDto(error.getField(), error.getDefaultMessage()))
//                .collect(Collectors.toList());
//    }
//
//    @ExceptionHandler(VacationApplicationNotFoundException.class)
//    public ResponseEntity<Object> handleNotFoundException(VacationApplicationNotFoundException e) {
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//    }

//    @ExceptionHandler(RuntimeException.class)
//    @ResponseStatus(INTERNAL_SERVER_ERROR)
//    public ErrorResponseDto buildApiErrorMessageDto(RuntimeException e) {
//        return ErrorResponseDto.builder()
//                .message(e.getLocalizedMessage())
//                .timestamp(String.valueOf(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()))
//                .build();
//    }

}
