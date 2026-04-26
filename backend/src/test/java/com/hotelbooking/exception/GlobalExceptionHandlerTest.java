package com.hotelbooking.exception;

import com.hotelbooking.util.ErrorResponseAssert;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Direct unit tests for {@link GlobalExceptionHandler} mapping rules.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn("/api/guests/99");
    }

    @Test
    void handleApiException_mapsStatusAndMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleApiException(new GuestNotFoundException(99L), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ErrorResponseAssert.assertThatError(response.getBody())
                .hasStatus(404)
                .hasMessageContaining("Guest not found")
                .hasPath("/api/guests/99");
    }

    @Test
    void handleValidationErrors_includesFieldMap() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("guestRequest", "email", "must not be blank")
        ));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationErrors(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().validationErrors()).containsEntry("email", "must not be blank");
        assertThat(response.getBody().message()).isEqualTo("Validation failed");
    }

    @Test
    void handleOptimisticLock_returnsConflict() {
        ResponseEntity<ErrorResponse> response = handler.handleOptimisticLock(
                new OptimisticLockingFailureException("version"),
                request
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        ErrorResponseAssert.assertThatError(response.getBody())
                .hasStatus(409)
                .hasMessageContaining("concurrently");
    }

    @Test
    void handleGenericException_hidesInternalDetails() {
        ResponseEntity<ErrorResponse> response =
                handler.handleGenericException(new RuntimeException("secret stack"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorResponseAssert.assertThatError(response.getBody())
                .hasStatus(500)
                .hasMessageContaining("unexpected");
        assertThat(response.getBody().message()).doesNotContain("secret stack");
    }
}
