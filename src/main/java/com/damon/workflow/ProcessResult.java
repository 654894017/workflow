package com.damon.workflow;


import com.damon.workflow.config.State;

import java.util.List;

public class ProcessResult {
    private boolean completed;
    private String processIdentifier;
    private State currentState;
    private List<State> nextStates;

    public ProcessResult(boolean completed, String processIdentifier, State currentState, List<State> nextStates) {
        this.completed = completed;
        this.processIdentifier = processIdentifier;
        this.currentState = currentState;
        this.nextStates = nextStates;
    }

    public boolean isCompleted() {
        return completed;
    }

    public String getProcessIdentifier() {
        return processIdentifier;
    }

    public State getCurrentState() {
        return currentState;
    }

    public List<State> getNextStates() {
        return nextStates;
    }
}
