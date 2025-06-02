package com.damon.workflow.config;

import com.damon.workflow.utils.CollUtils;
import com.damon.workflow.utils.StrUtils;

import java.util.*;


public class ProcessDefinition {
    private String id;
    private String version;
    private String startStateId;
    private List<State> states;
    private Map<String, Set<String>> stateInverseRelationMap;

    public String getIdentifier() {
        return id + ":" + version;
    }

    public State getState(String id) {
        return states.stream()
                .filter(state -> state.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("State not found: " + id));
    }

    public Map<String, Set<String>> getStateInverseRelation() {
        return stateInverseRelationMap;
    }

    /**
     * 构建状态的逆向关系
     *
     * @param states
     * @return
     */
    private Map<String, Set<String>> createStateInverseRelation(List<State> states) {
        Map<String, Set<String>> stateInverseRelationMap = new HashMap<>();
        for (State state : states) {
            String nextStateId = state.getNextStateId();
            if (StrUtils.isNotEmpty(nextStateId)) {
                Set<String> inputStateIds = stateInverseRelationMap.computeIfAbsent(
                        nextStateId, s -> new HashSet<>()
                );
                inputStateIds.add(state.getId());
            } else {
                if (CollUtils.isNotEmpty(state.getConditions())) {
                    for (Condition condition : state.getConditions()) {
                        String conditionNextStateId = condition.getNextStateId();
                        if (StrUtils.isNotEmpty(conditionNextStateId)) {
                            Set<String> inputStateIds = stateInverseRelationMap.computeIfAbsent(
                                    conditionNextStateId, s -> new HashSet<>()
                            );
                            inputStateIds.add(state.getId());
                        }
                    }
                }
            }
        }
        return stateInverseRelationMap;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStartStateId() {
        return startStateId;
    }

    public void setStartStateId(String startStateId) {
        this.startStateId = startStateId;
    }

    public List<State> getStates() {
        return states;
    }

    public void setStates(List<State> states) {
        this.stateInverseRelationMap = createStateInverseRelation(states);
        this.states = states;
    }

    public Map<String, Set<String>> getStateInverseRelationMap() {
        return stateInverseRelationMap;
    }

    public void setStateInverseRelationMap(Map<String, Set<String>> stateInverseRelationMap) {
        this.stateInverseRelationMap = stateInverseRelationMap;
    }
}
