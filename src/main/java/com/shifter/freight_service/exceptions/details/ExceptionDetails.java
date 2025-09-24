package com.shifter.freight_service.exceptions.details;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ExceptionDetails {

    protected String title;

    protected String details;

    protected String developerMessage;

    protected int status;

    protected String timestamp;

    public static ExceptionDetails createExceptionDetails(Exception ex, HttpStatusCode statusCode) {
        return createExceptionDetails(ex, statusCode, "Internal Error.");
    }

    public static ExceptionDetails createExceptionDetails(Exception ex, HttpStatusCode statusCode, String exTitle) {
        Throwable cause = ex.getCause();
        log.info("stacktrace is");
        ex.printStackTrace();
        String title = cause != null
                ? cause.getMessage()
                : exTitle;

        return ExceptionDetails
                .builder()
                .status(statusCode.value())
                .title(title)
                .details(ex.getMessage())
                .developerMessage(ex.getClass().getName())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

}