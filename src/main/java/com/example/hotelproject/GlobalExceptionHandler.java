package com.example.hotelproject;

import com.example.hotelproject.availability.exception.BlockedDateNotFoundException;
import com.example.hotelproject.availability.exception.InvalidDateRangeException;
import com.example.hotelproject.availability.exception.PricingRuleNotFoundException;
import com.example.hotelproject.availability.exception.RoomNotAvailableException;
import com.example.hotelproject.catalog.hotel.HotelNotFoundException;
import com.example.hotelproject.catalog.room.RoomTypeNotFoundException;
import com.example.hotelproject.booking.exception.BookingNotFoundException;
import com.example.hotelproject.booking.exception.InvalidBookingStateException;
import com.example.hotelproject.notification.exception.NotificationFailedException;
import com.example.hotelproject.payment.exception.PaymentFailedException;
import com.example.hotelproject.payment.exception.PaymentNotFoundException;
import com.example.hotelproject.review.exception.ReviewNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Catalog exceptions
    @ExceptionHandler(HotelNotFoundException.class)
    public ResponseEntity<ApiError> handleHotelNotFound(
            HotelNotFoundException ex,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(RoomTypeNotFoundException.class)
    public ResponseEntity<ApiError> handleRoomTypeNotFound(
            RoomTypeNotFoundException ex,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // Availability exceptions
    @ExceptionHandler(RoomNotAvailableException.class)
    public ResponseEntity<ApiError> handleRoomNotAvailable(
            RoomNotAvailableException ex,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.CONFLICT.value(),
                "Room Not Available",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ApiError> handleInvalidDateRange(
            InvalidDateRangeException ex,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Date Range",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(PricingRuleNotFoundException.class)
    public ResponseEntity<ApiError> handlePricingRuleNotFound(
            PricingRuleNotFoundException ex,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.NOT_FOUND.value(),
                "Pricing Rule Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(BlockedDateNotFoundException.class)
    public ResponseEntity<ApiError> handleBlockedDateNotFound(
            BlockedDateNotFoundException ex,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.NOT_FOUND.value(),
                "Blocked Date Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // Booking exceptions (أضف هذين)
    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ApiError> handleBookingNotFound(
            BookingNotFoundException ex,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.NOT_FOUND.value(),
                "Booking Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(InvalidBookingStateException.class)
    public ResponseEntity<ApiError> handleInvalidBookingState(
            InvalidBookingStateException ex,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.CONFLICT.value(),
                "Invalid Booking State",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // Validation exception
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        List<Map<String, String>> fieldErrors = new ArrayList<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            Map<String, String> errorDetails = new HashMap<>();
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errorDetails.put("field", fieldName);
            errorDetails.put("message", errorMessage);
            fieldErrors.add(errorDetails);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Bad Request");
        response.put("message", "Validation failed");
        response.put("path", request.getRequestURI());
        response.put("errors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // FIX: 11 IllegalStateException now returns 400 instead of 500
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // FIX: 12 DateTimeParseException now returns 400 with a clear format hint
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ApiError> handleDateTimeParse(
            DateTimeParseException ex,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Invalid date format. Use yyyy-MM-dd",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(
            org.springframework.security.authentication.BadCredentialsException ex,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Invalid username or password",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        ex.printStackTrace();
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Something went wrong",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<ApiError> handleReviewNotFound(
            ReviewNotFoundException ex,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.NOT_FOUND.value(),
                "Review Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ApiError> handlePaymentFailed(
            PaymentFailedException ex,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.PAYMENT_REQUIRED.value(),
                "Payment Failed",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(body);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ApiError> handlePaymentNotFound(
            PaymentNotFoundException ex,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.NOT_FOUND.value(),
                "Payment Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(NotificationFailedException.class)
    public ResponseEntity<ApiError> handleNotificationFailed(
            NotificationFailedException ex,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Notification Failed",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(com.example.hotelproject.auth.exception.UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(
            com.example.hotelproject.auth.exception.UserNotFoundException ex,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.NOT_FOUND.value(),
                "User Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(com.example.hotelproject.auth.exception.UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(
            com.example.hotelproject.auth.exception.UnauthorizedException ex,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(com.example.hotelproject.auth.exception.RefreshTokenException.class)
    public ResponseEntity<ApiError> handleRefreshTokenException(
            com.example.hotelproject.auth.exception.RefreshTokenException ex,
            HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.UNAUTHORIZED.value(),
                "Refresh Token Error",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex,
            HttpServletRequest request) {

        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.FORBIDDEN.value(),
                "Access Denied",
                "You do not have permission to access this resource",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(org.springframework.security.authorization.AuthorizationDeniedException.class)
    public ResponseEntity<ApiError> handleAuthorizationDeniedException(
            org.springframework.security.authorization.AuthorizationDeniedException ex,
            HttpServletRequest request) {

        ApiError body = new ApiError(
                Instant.now().toString(),
                HttpStatus.FORBIDDEN.value(),
                "Access Denied",
                "You do not have permission to access this resource",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
}