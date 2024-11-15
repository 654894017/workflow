package com.damon.workflow;


import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;

import java.util.Map;

public class RuntimeContext {
    private final ProcessDefinition processDefinition;
    private final State currentState;
    private final Map<String, Object> variables;

    public RuntimeContext(ProcessDefinition processDefinition, State currentState, Map<String, Object> variables) {

        this.processDefinition = processDefinition;
        this.currentState = currentState;
        this.variables = variables;
    }

    public State getCurrentState() {
        return currentState;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public ProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

}