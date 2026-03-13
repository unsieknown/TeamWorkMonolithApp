package com.mordiniaa.backend.exceptions;

import com.mordiniaa.backend.payload.ApiExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.management.relation.RelationNotFoundException;

@RestControllerAdvice
public class GlobalExceptionsHandler {

    @ExceptionHandler({
            UnsupportedOperationException.class,
            BadRequestException.class,
            TaskAlreadyUpdatedException.class
    })
    public ResponseEntity<ApiExceptionResponse> unsupportedOperation(RuntimeException e) {
        String message = e.getMessage();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return exceptionResponse(message, status);
    }

    @ExceptionHandler(UnexpectedException.class)
    public ResponseEntity<ApiExceptionResponse> unexpectedException(UnexpectedException e) {
        String message = e.getMessage();
        int status = e.getStatus();
        return exceptionResponse(message, status);
    }

    @ExceptionHandler(ArgumentNotPresentException.class)
    public ResponseEntity<ApiExceptionResponse> argumentNotPresentException(ArgumentNotPresentException e) {
        String message = e.getMessage();
        int status = e.getStatus();
        return exceptionResponse(message, status);
    }

    @ExceptionHandler({
            SessionExpiredException.class,
            InvalidJwtException.class,
            RefreshTokenException.class,
            SessionException.class
    })
    public ResponseEntity<ApiExceptionResponse> sessionExpiredException(RuntimeException e) {
        String message = e.getMessage();
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return exceptionResponse(message, status);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiExceptionResponse> badCredentialsException(BadCredentialsException e) {
        String message = "Bad Credentials";
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return exceptionResponse(message, status);
    }

    @ExceptionHandler({
            TeamNotFoundException.class,
            BoardNotFoundException.class,
            RelationNotFoundException.class,
            TaskCategoryNotFoundException.class,
            NoteNotFoundException.class,
            FileNodeNotFound.class,
            ImageNotFoundException.class,
            TaskCommentNotFound.class,
            TaskNotFoundException.class,
            RoleNotFoundException.class
    })
    public ResponseEntity<ApiExceptionResponse> notFoundException(RuntimeException e) {
        String message = e.getMessage();
        HttpStatus status = HttpStatus.NOT_FOUND;
        return exceptionResponse(message, status);
    }

    @ExceptionHandler(UserNotInTeamException.class)
    public ResponseEntity<ApiExceptionResponse> userNotInTeamException(UserNotInTeamException e) {
        String message = e.getMessage();
        int status = e.getStatus();
        return exceptionResponse(message, status);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiExceptionResponse> accessDeniedException(AccessDeniedException e) {
        String message = e.getMessage();
        HttpStatus status = HttpStatus.FORBIDDEN;
        return exceptionResponse(message, status);
    }

    @ExceptionHandler(StorageQuotaExceededException.class)
    public ResponseEntity<ApiExceptionResponse> storageQuotaExceededException(StorageQuotaExceededException e) {
        String message = e.getMessage();
        HttpStatus status = HttpStatus.PAYLOAD_TOO_LARGE;
        return exceptionResponse(message, status);
    }

    @ExceptionHandler(AddressValidationException.class)
    public ResponseEntity<ApiExceptionResponse> addressValidationException(AddressValidationException e) {
        String message = e.getMessage();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return exceptionResponse(message, status);
    }

    @ExceptionHandler(ContactDataValidationException.class)
    public ResponseEntity<ApiExceptionResponse> contactDataValidationException(ContactDataValidationException e) {
        String message = e.getMessage();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return exceptionResponse(message, status);
    }

    private ResponseEntity<ApiExceptionResponse> exceptionResponse(String message, int status) {
        return exceptionResponse(message, HttpStatus.valueOf(status));
    }

    private ResponseEntity<ApiExceptionResponse> exceptionResponse(String message, HttpStatus status) {
        return new ResponseEntity<>(
                new ApiExceptionResponse(
                        status.value(),
                        message
                ),
                status
        );
    }
}
