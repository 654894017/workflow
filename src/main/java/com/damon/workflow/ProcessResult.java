package com.damon.workflow;


import com.damon.workflow.config.State;

import java.util.List;

public class ProcessResult {
    private boolean completed;
    private State currentState;
    private List<State> nextStates;

    public ProcessResult(boolean completed, State currentState, List<State> nextStates) {
        this.completed = completed;
        this.nextStates = nextStates;
        this.currentState = currentState;
    }


    public ProcessResult(State currentState, List<State> nextStates) {
        this.nextStates = nextStates;
        this.currentState = currentState;
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
}
