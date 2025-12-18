package com.damon.workflow;


import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.StateIdentifier;

import java.util.Map;

/**
 * 流程上下文
 * <p>
 * damon
 */
public class RuntimeContext {
    private final ProcessDefinition definition;
    private final StateIdentifier currentStateIdentifier;
    private final Map<String, Object> variables;
    private final String businessId;

    public RuntimeContext(
            ProcessDefinition definition,
            StateIdentifier currentStateIdentifier,
            Map<String, Object> variables,
            String businessId
    ) {
        this.businessId = businessId;
        this.definition = definition;
        this.currentStateIdentifier = currentStateIdentifier;
        this.variables = variables;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setResult(Object result) {
        variables.put(currentStateIdentifier.getFullPaths(), result);
    }

    public ProcessDefinition getDefinition() {
        return definition;
    }

    public StateIdentifier getCurrentStateIdentifier() {
        return currentStateIdentifier;
    }

    public String getBusinessId() {
        return businessId;
    }
}