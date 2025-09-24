package com.shifter.freight_service.exceptions;

public class RemoteResourceNotFoundException extends RuntimeException {
    public RemoteResourceNotFoundException(String message, Throwable cause) { super(message, cause); }
    public RemoteResourceNotFoundException(String message) { super(message); }
}
