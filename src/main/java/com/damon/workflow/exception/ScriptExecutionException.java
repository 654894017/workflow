package com.damon.workflow.exception;

public class ScriptExecutionException extends ProcessException {
    public ScriptExecutionException() {
    }

    public ScriptExecutionException(Throwable cause) {
        super(cause);
    }

    public ScriptExecutionException(String message) {
        super(message);
    }

    public ScriptExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScriptExecutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
