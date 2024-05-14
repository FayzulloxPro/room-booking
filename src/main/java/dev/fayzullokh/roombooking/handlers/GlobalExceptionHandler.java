package dev.fayzullokh.roombooking.handlers;

import dev.fayzullokh.roombooking.dtos.AppErrorDTO;
import dev.fayzullokh.roombooking.exceptions.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AppErrorDTO> handleUnknownExceptions(Exception e, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(
                new AppErrorDTO(
                        request.getRequestURI(),
                        e.getMessage(),
                        null,
                        400
                )
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<AppErrorDTO> handleRuntimeExceptions(RuntimeException e, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(
                new AppErrorDTO(
                        request.getRequestURI(),
                        e.getMessage(),
                        null,
                        400
                )
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<AppErrorDTO> handleItemNotFoundException(NotFoundException e, HttpServletRequest request) {
        return ResponseEntity.status(404)
                .body(new AppErrorDTO(request.getRequestURI(), e.getMessage(), 404));
    }


    @ExceptionHandler(CredentialsExpiredException.class)
    public ResponseEntity<AppErrorDTO> handleCredentialsExpiredException(CredentialsExpiredException e, HttpServletRequest request) {
        return ResponseEntity.status(401)
                .body(new AppErrorDTO(request.getRequestURI(), e.getMessage(), 400));
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<AppErrorDTO> handleInsufficientAuthenticationException(InsufficientAuthenticationException e, HttpServletRequest request) {
        return ResponseEntity.status(403)
                .body(new AppErrorDTO(request.getRequestURI(), e.getMessage(), 400));
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AppErrorDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String errorMessage = "Input is not valid";
        Map<String, List<String>> errorBody = new HashMap<>();
        for (FieldError fieldError : e.getFieldErrors()) {
            String field = fieldError.getField();
            String message = fieldError.getDefaultMessage();
            errorBody.compute(field, (s, values) -> {
                if (!Objects.isNull(values))
                    values.add(message);
                else
                    values = new ArrayList<>(Collections.singleton(message));
                return values;
            });
        }
        String errorPath = request.getRequestURI();
        AppErrorDTO errorDTO = new AppErrorDTO(errorPath, errorMessage, errorBody, 400);
        return ResponseEntity.status(400).body(errorDTO);
    }

}
