package com.damon.workflow.config;

import com.damon.workflow.utils.CollUtils;
import com.damon.workflow.utils.StrUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
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

    public void setStates(List<State> states) {
        this.stateInverseRelationMap = createStateInverseRelation(states);
        this.states = states;
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
                Set<String> inputStateIds = stateInverseRelationMap.computeIfAbsent(nextStateId, s -> new HashSet<>());
                inputStateIds.add(state.getId());
            } else {
                if (CollUtils.isNotEmpty(state.getConditions())) {
                    for (Condition condition : state.getConditions()) {
                        String conditionNextStateId = condition.getNextStateId();
                        if (StrUtils.isNotEmpty(conditionNextStateId)) {
                            Set<String> inputStateIds = stateInverseRelationMap.computeIfAbsent(conditionNextStateId, s -> new HashSet<>());
                            inputStateIds.add(state.getId());
                        }
                    }
                }
            }
        }
        return stateInverseRelationMap;
    }
}
