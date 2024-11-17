package com.damon.workflow.exception;

public class ProcessTaskException extends RuntimeException {

    public ProcessTaskException() {
        super();
    }

    public ProcessTaskException(Throwable cause) {
        super(cause);
    }

    public ProcessTaskException(String message) {
        super(message);
    }

    public ProcessTaskException(String message, Throwable cause) {
        super(message, cause);
    }

    protected ProcessTaskException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
