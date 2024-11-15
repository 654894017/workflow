package com.damon.workflow.exception;

public class ProcessException extends RuntimeException {

    public ProcessException() {
        super();
    }

    public ProcessException(Throwable cause) {
        super(cause);
    }

    public ProcessException(String message) {
        super(message);
    }

    public ProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ProcessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
