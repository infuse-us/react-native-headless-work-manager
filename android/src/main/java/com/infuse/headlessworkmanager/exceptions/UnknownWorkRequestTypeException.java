package com.infuse.headlessworkmanager.exceptions;

public class UnknownWorkRequestTypeException extends Exception {
    public UnknownWorkRequestTypeException(String workRequestType) {
        super("Unknown work request type: " + workRequestType);
    }
}
