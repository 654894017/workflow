package com.damon.workflow;


import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.StateIdentifier;

import java.util.Map;

public class RuntimeContext {
    private final ProcessDefinition processDefinition;
    private final StateIdentifier currentStateIdentifier;
    private final Map<String, Object> variables;
    private final String businessId;

    public RuntimeContext(
            ProcessDefinition processDefinition,
            StateIdentifier currentStateIdentifier,
            Map<String, Object> variables,
            String businessId
    ) {
        this.businessId = businessId;
        this.processDefinition = processDefinition;
        this.currentStateIdentifier = currentStateIdentifier;
        this.variables = variables;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setResult(Object result) {
        variables.put(currentStateIdentifier.getFullPaths(), result);
    }

    public ProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public StateIdentifier getCurrentStateIdentifier() {
        return currentStateIdentifier;
    }

    public String getBusinessId() {
        return businessId;
    }
}