package com.damon.workflow.config;

import java.util.List;

public class ProcessDefinition {
    private String id;
    private String startState;
    private List<State> states;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStartState() {
        return startState;
    }

    public void setStartState(String startState) {
        this.startState = startState;
    }

    public List<State> getStates() {
        return states;
    }

    public void setStates(List<State> states) {
        this.states = states;
    }

    public State getState(String id) {
        return states.stream()
                .filter(state -> state.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("State not found: " + id));
    }
}
