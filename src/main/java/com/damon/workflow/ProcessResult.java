package com.damon.workflow;


import com.damon.workflow.config.State;

import java.util.List;

public class ProcessResult {
    private boolean completed;
    private State currentState;
    private List<State> nextStates;
    private Object result;

    public ProcessResult(boolean completed, State currentState, List<State> nextStates, Object result) {
        this.completed = completed;
        this.nextStates = nextStates;
        this.currentState = currentState;
        this.result = result;
    }


    public ProcessResult(State currentState, List<State> nextStates, Object result) {
        this.nextStates = nextStates;
        this.currentState = currentState;
        this.result = result;
    }

    public boolean isCompleted() {
        return completed;
    }

    public State getCurrentState() {
        return currentState;
    }

    public List<State> getNextStates() {
        return nextStates;
    }

    public <T> T getResult() {
        return (T) result;
    }
}
