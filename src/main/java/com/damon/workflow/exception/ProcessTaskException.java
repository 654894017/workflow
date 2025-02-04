package com.damon.workflow.exception;

import com.damon.workflow.config.StateIdentifier;

public class ProcessTaskException extends RuntimeException {

    private StateIdentifier exceptionStateIdentifier;

    public ProcessTaskException() {
        super();
    }

    public ProcessTaskException(StateIdentifier exceptionStateIdentifier, Throwable cause) {
        super(cause);
        this.exceptionStateIdentifier = exceptionStateIdentifier;
    }

    public ProcessTaskException(String message) {
        super(message);
    }

    public ProcessTaskException(StateIdentifier exceptionStateIdentifier, String message, Throwable cause) {
        super(message, cause);
        this.exceptionStateIdentifier = exceptionStateIdentifier;
    }

    protected ProcessTaskException(StateIdentifier exceptionStateIdentifier, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.exceptionStateIdentifier = exceptionStateIdentifier;
    }

    public StateIdentifier getExceptionStateIdentifier() {
        return exceptionStateIdentifier;
    }
}
