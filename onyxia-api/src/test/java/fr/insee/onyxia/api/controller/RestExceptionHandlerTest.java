package fr.insee.onyxia.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;

class RestExceptionHandlerTest {

    private RestExceptionHandler restExceptionHandler;
    private HttpServletRequest mockRequest;

    @BeforeEach
    void setUp() {
        restExceptionHandler = new RestExceptionHandler();
        mockRequest = Mockito.mock(HttpServletRequest.class);
    }

    @Test
    void testHandleAccessDeniedForNamespaceCreation() {
        when(mockRequest.getRequestURI()).thenReturn("/onboarding");

        AccessDeniedException exception =
                new AccessDeniedException("Access Denied for Namespace Creation");
        ProblemDetail result =
                restExceptionHandler.handleAccessDeniedException(exception, mockRequest);

        assertEquals(HttpStatus.FORBIDDEN.value(), result.getStatus());
        assertEquals(RestExceptionTypes.ACCESS_DENIED, result.getType());
        assertEquals("Access Denied", result.getTitle());
        assertEquals("You do not have permission to access this resource.", result.getDetail());
        assertEquals(URI.create("/onboarding"), result.getInstance());
    }

    @Test
    void testHandleValidationException() {
        when(mockRequest.getRequestURI()).thenReturn("/my-lab/app");

        ValidationException validationException = Mockito.mock(ValidationException.class);
        when(validationException.getMessage()).thenReturn("Validation Error Message");

        ProblemDetail result =
                restExceptionHandler.handleValidationException(validationException, mockRequest);

        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
        assertEquals(RestExceptionTypes.VALIDATION_FAILED, result.getType());
        assertEquals("Validation Failed", result.getTitle());
        assertEquals("The request contains invalid data.", result.getDetail());
        assertEquals(URI.create("/my-lab/app"), result.getInstance());
        assertThat(result.getProperties()).containsKey("errors");
    }

    @Test
    void testHandleValidationExceptionWithErrors() {
        when(mockRequest.getRequestURI()).thenReturn("/my-lab/app");

        Schema mockSchema = Mockito.mock(Schema.class);

        ValidationException ex1 =
                new ValidationException(mockSchema, "Field 'name' is required", "name");
        ValidationException ex2 =
                new ValidationException(mockSchema, "Field 'version' must be a number", "version");

        ValidationException validationException = Mockito.mock(ValidationException.class);
        when(validationException.getMessage()).thenReturn("Validation Error Message");
        when(validationException.getCausingExceptions()).thenReturn(List.of(ex1, ex2));

        ProblemDetail result =
                restExceptionHandler.handleValidationException(validationException, mockRequest);

        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
        assertEquals(RestExceptionTypes.VALIDATION_FAILED, result.getType());
        assertEquals("Validation Failed", result.getTitle());
        assertEquals("The request contains invalid data.", result.getDetail());
        assertEquals(URI.create("/my-lab/app"), result.getInstance());

        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) result.getProperties().get("errors");
        assertThat(errors)
                .containsExactly(
                        "#: Field 'name' is required", "#: Field 'version' must be a number");
    }
}
