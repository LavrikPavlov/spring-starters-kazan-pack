package ru.kazan.errorhandlerspringstarter.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.kazan.errorhandlerspringstarter.dto.ErrorResponse;
import ru.kazan.errorhandlerspringstarter.dto.enums.ExceptionEnum;

import java.security.SignatureException;
import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    private static final String REASON_OF_EXCEPTION = "\033[1;97mAn invalid request with error \033[1;31m[ {} ]" +
            " \033[1;97mwas rejected because\n {}";

    @ExceptionHandler({HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> catchMessageNotReadableException(HttpServletRequest request) {
        ExceptionEnum exceptionMessage = ExceptionEnum.UNSUPPORTED_MEDIA_TYPE;
        ErrorResponse response = getExceptionResponse(request, exceptionMessage);
        return new ResponseEntity<>(response, exceptionMessage.getHttpStatus());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> catchMethodNotAllowed(HttpServletRequest request) {
        ExceptionEnum exceptionMessage = ExceptionEnum.METHOD_NOT_ALLOWED;
        ErrorResponse response = getExceptionResponse(request, exceptionMessage);
        return new ResponseEntity<>(response, exceptionMessage.getHttpStatus());
    }

    @ExceptionHandler({
            SignatureException.class,
    })
    public ResponseEntity<ErrorResponse> catchUnauthorized(HttpServletRequest request) {
        ExceptionEnum exceptionMessage = ExceptionEnum.UNAUTHORIZED;
        ErrorResponse response = getExceptionResponse(request, exceptionMessage);
        return new ResponseEntity<>(response, exceptionMessage.getHttpStatus());
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            IllegalArgumentException.class,
            NullPointerException.class,
            MissingRequestHeaderException.class
    })
    public ResponseEntity<ErrorResponse> catchMethodArgumentTypeMismatchException(HttpServletRequest request) {
        ExceptionEnum exceptionMessage = ExceptionEnum.BAD_REQUEST;
        ErrorResponse response = getExceptionResponse(request, exceptionMessage);
        return new ResponseEntity<>(response, exceptionMessage.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> catchOtherException(HttpServletRequest request) {
        ExceptionEnum exceptionMessage = ExceptionEnum.INTERNAL_SERVER_ERROR;
        ErrorResponse response = getExceptionResponse(request, exceptionMessage);
        return new ResponseEntity<>(response, exceptionMessage.getHttpStatus());
    }

    private ErrorResponse getExceptionResponse(HttpServletRequest request, ExceptionEnum exceptionEnum) {

        ErrorResponse response = new ErrorResponse(
                request.getRequestURI(),
                exceptionEnum.getHttpStatus().value(),
                exceptionEnum.getHttpStatus().getReasonPhrase(),
                exceptionEnum.getErrorMessage(),
                LocalDateTime.now()
        );

        log.error(REASON_OF_EXCEPTION, response.getError(), response.getMessage());

        return response;
    }
}
