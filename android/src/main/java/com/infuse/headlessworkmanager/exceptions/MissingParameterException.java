package com.infuse.headlessworkmanager.exceptions;

public class MissingParameterException extends Exception {
    public MissingParameterException(String parameter) {
        super("Missing parameter: " + parameter);
    }
}
