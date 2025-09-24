package com.shifter.freight_service.exceptions;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message, Throwable cause) { super(message, cause); }
}
