package com.damon.workflow.config;

public class NextState {
    private String nextStateFullPaths;
    private State nextState;

    public NextState(String nextStateFullPaths, State nextState) {
        this.nextStateFullPaths = nextStateFullPaths;
        this.nextState = nextState;
    }

    public StateIdentifier getNextStateIdentifier() {
        return StateIdentifier.buildFromFullPaths(nextStateFullPaths);
    }

    public String getNextStateFullPaths() {
        return nextStateFullPaths;
    }

    public void setNextStateFullPaths(String nextStateFullPaths) {
        this.nextStateFullPaths = nextStateFullPaths;
    }

    public State getNextState() {
        return nextState;
    }

    public void setNextState(State nextState) {
        this.nextState = nextState;
    }
}
