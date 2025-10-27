package com.example.bankcards.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(NotFound ex) {
        logger.warn("Resource not found: {}", ex.getMessage());
        return new ErrorResponse("NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyExist.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleUserAlreadyExist(UserAlreadyExist ex) {
        logger.warn("User already exists: {}", ex.getMessage());
        return new ErrorResponse("USER_ALREADY_EXISTS", ex.getMessage());
    }

    @ExceptionHandler(UserDoesNotExistOrPasswordIncorrect.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUserDoesNotExist(UserDoesNotExistOrPasswordIncorrect ex) {
        logger.warn("Authentication failed: {}", ex.getMessage());
        return new ErrorResponse("AUTHENTICATION_FAILED", ex.getMessage());
    }

    @ExceptionHandler(PasswordIsNotValid.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handlePasswordNotValid(PasswordIsNotValid ex) {
        logger.warn("Password validation failed: {}", ex.getMessage());
        return new ErrorResponse("INVALID_PASSWORD", ex.getMessage());
    }

    @ExceptionHandler(RefreshTokenExpired.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleRefreshTokenExpired(RefreshTokenExpired ex) {
        logger.warn("Refresh token expired: {}", ex.getMessage());
        return new ErrorResponse("REFRESH_TOKEN_EXPIRED", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logger.warn("Validation exception: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDenied(AccessDeniedException ex) {
        logger.warn("Access denied: {}", ex.getMessage());
        return new ErrorResponse("ACCESS_DENIED", "You don't have permission to access this resource");
    }


    @ExceptionHandler(TransactedMoneyIsNegativeOrZero.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleTransactedMoneyIsNegativeOrZero(TransactedMoneyIsNegativeOrZero ex){
        return new ErrorResponse("NEGATIVE_NUMBER", "Entered negative money to transact");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex) {
        logger.error("Internal server error: ", ex);
        return new ErrorResponse("INTERNAL_ERROR", "An internal server error occurred");
    }

}