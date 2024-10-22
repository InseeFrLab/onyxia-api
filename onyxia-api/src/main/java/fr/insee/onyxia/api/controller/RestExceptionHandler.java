package fr.insee.onyxia.api.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.everit.json.schema.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(Exception ignored) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problemDetail.setTitle("Access denied");
        return problemDetail;
    }

    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleValidationException(ValidationException ex) {
        List<String> errors =
                ex.getCausingExceptions().stream()
                        .map(ValidationException::getMessage)
                        .collect(Collectors.toList());

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setProperties(Map.of("errors", errors));
        problemDetail.setTitle("Validation failed");
        return problemDetail;
    }
}
