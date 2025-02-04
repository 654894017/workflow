package com.damon.workflow;


import com.damon.workflow.config.NextState;
import com.damon.workflow.config.StateIdentifier;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ComplexProcessResult {
    private boolean completed;
    private StateIdentifier currentStateIdentifier;
    private List<NextState> nextStates;
    private Object result;
    private Map<String, Object> statesProcessResult = new HashMap<>();

    public ComplexProcessResult(boolean completed, List<NextState> nextStates) {
        this.completed = completed;
        this.nextStates = nextStates;
    }

    public <T> T getStateProcessResult() {
        return (T) result;
    }

    public void setStateProcessResult(Object result) {
        this.result = result;
    }
}
