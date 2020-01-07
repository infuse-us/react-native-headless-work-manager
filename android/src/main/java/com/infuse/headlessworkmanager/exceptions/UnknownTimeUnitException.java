package com.infuse.headlessworkmanager.exceptions;

public class UnknownTimeUnitException extends Exception {
    public UnknownTimeUnitException(String timeUnit) {
        super("Unknown time unit: " + timeUnit);
    }
}
