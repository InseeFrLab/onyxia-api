package fr.insee.onyxia.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.everit.json.schema.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    // Helper method to create ProblemDetail
    private ProblemDetail createProblemDetail(
            HttpStatus status, URI type, String title, String detail, String instancePath) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setType(type);
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        problemDetail.setInstance(URI.create(instancePath));
        return problemDetail;
    }

    // AccessDeniedException handler
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(
            AccessDeniedException exception, HttpServletRequest request) {
        return createProblemDetail(
                HttpStatus.FORBIDDEN,
                RestExceptionTypes.ACCESS_DENIED,
                "Access Denied",
                "You do not have permission to access this resource.",
                request.getRequestURI());
    }

    // ValidationException handler
    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleValidationException(
            ValidationException ex, HttpServletRequest request) {
        List<String> errors =
                ex.getCausingExceptions() != null && !ex.getCausingExceptions().isEmpty()
                        ? ex.getCausingExceptions().stream()
                                .map(ValidationException::getMessage)
                                .collect(Collectors.toList())
                        : List.of(ex.getMessage()); // Fallback to the main exception message if
        // causing exceptions are empty.

        ProblemDetail problemDetail =
                createProblemDetail(
                        HttpStatus.BAD_REQUEST,
                        RestExceptionTypes.VALIDATION_FAILED,
                        "Validation Failed",
                        "The request contains invalid data.",
                        request.getRequestURI());

        // Add 'errors' as a custom property
        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }
}
