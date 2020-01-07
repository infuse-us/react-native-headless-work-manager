package com.infuse.headlessworkmanager.exceptions;

public class UnknownExistingWorkPolicyException extends Exception {
    public UnknownExistingWorkPolicyException(String existingWorkPolicy) {
        super("Unknown existing work policy: " + existingWorkPolicy);
    }
}
