package iums.configuration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import iums.exception.InvalidDateException;
import iums.exception.InvalidDatetimeException;
import iums.exception.InvalidPaginationException;
import iums.exception.InvalidUsernameException;
import iums.exception.UserNotFoundException;
import iums.resource.ErrorResponse;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(InvalidDateException.class)
    public ResponseEntity<ErrorResponse> invalidDate() {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of("invalid date"));
    }

    @ExceptionHandler(InvalidDatetimeException.class)
    public ResponseEntity<ErrorResponse> invalidDatetime() {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of("invalid datetime"));
    }

    @ExceptionHandler(InvalidPaginationException.class)
    public ResponseEntity<ErrorResponse> invalidPagination() {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of("invalid pagination"));
    }

    @ExceptionHandler(InvalidUsernameException.class)
    public ResponseEntity<ErrorResponse> invalidUsername() {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ErrorResponse.of("invalid username"));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> userNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("user not found"));
    }
}
