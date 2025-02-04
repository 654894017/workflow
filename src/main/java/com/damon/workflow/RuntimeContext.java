package com.damon.workflow;


import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.StateIdentifier;
import lombok.Getter;

import java.util.*;

@Getter
public class RuntimeContext {
    private final ProcessDefinition processDefinition;
    private final StateIdentifier currentStateIdentifier;
    private final Map<String, Object> variables;
    private final String businessId;

    public RuntimeContext(ProcessDefinition processDefinition, StateIdentifier currentStateIdentifier, Map<String, Object> variables, String businessId) {
        this.businessId = businessId;
        this.processDefinition = processDefinition;
        this.currentStateIdentifier = currentStateIdentifier;
        this.variables = variables;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public <T> T getStateProcessResult() {
        Map<String, Object> statesProcessResult = (Map<String, Object>) variables.get(ProcessConstant.STATES_PROCESS_RESULT);
        return (T) statesProcessResult.get(currentStateIdentifier.getFullPaths());
    }

    public synchronized void setStateProcessResult(Object result) {
        Map<String, Object> statesProcessResult = (Map<String, Object>) variables.computeIfAbsent(ProcessConstant.STATES_PROCESS_RESULT,
                key -> Collections.synchronizedMap(new HashMap<>())
        );
        statesProcessResult.put(currentStateIdentifier.getFullPaths(), result);
    }

    public synchronized void setStateProcessFailed() {
        List<StateIdentifier> exceptions = (List<StateIdentifier>) variables.computeIfAbsent(ProcessConstant.STATES_PROCESS_FAILED,
                key -> Collections.synchronizedList(new ArrayList<>())
        );
        exceptions.add(currentStateIdentifier);
    }
}