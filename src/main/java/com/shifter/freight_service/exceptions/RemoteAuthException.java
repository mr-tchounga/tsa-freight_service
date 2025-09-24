package com.shifter.freight_service.exceptions;

public class RemoteAuthException extends RuntimeException {
    public RemoteAuthException(String message) { super(message); }
    public RemoteAuthException(String message, Throwable cause) { super(message, cause); }
}
