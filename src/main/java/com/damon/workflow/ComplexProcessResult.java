package com.damon.workflow;


import com.damon.workflow.config.NextState;
import com.damon.workflow.config.StateIdentifier;

import java.util.List;


public class ComplexProcessResult {
    private boolean completed;
    private StateIdentifier currentStateIdentifier;
    private List<NextState> nextStates;
    private Object result;

    public ComplexProcessResult(boolean completed, List<NextState> nextStates) {
        this.completed = completed;
        this.nextStates = nextStates;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public StateIdentifier getCurrentStateIdentifier() {
        return currentStateIdentifier;
    }

    public void setCurrentStateIdentifier(StateIdentifier currentStateIdentifier) {
        this.currentStateIdentifier = currentStateIdentifier;
    }

    public List<NextState> getNextStates() {
        return nextStates;
    }

    public void setNextStates(List<NextState> nextStates) {
        this.nextStates = nextStates;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
