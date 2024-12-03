package com.damon.workflow.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProcessDefinition {
    private String id;
    private String version;
    private String startStateId;
    private List<State> states;

    public String getIdentifier() {
        return id + ":" + version;
    }

    public State getState(String id) {
        return states.stream()
                .filter(state -> state.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("State not found: " + id));
    }
}
