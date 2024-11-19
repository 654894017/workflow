package com.damon.workflow.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
public class State {
    private String id;
    private String type;
    private String name;
    private String nextState;
    private Map<String, Object> extendParams;
    private List<Condition> conditions;
    private String nextStateCondition;
    private String nextStateConditionScriptType;
    private String nextStateConditionParser;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        State state = (State) o;
        return Objects.equals(id, state.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
