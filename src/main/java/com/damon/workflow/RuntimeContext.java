package com.damon.workflow;


import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class RuntimeContext {
    private final ProcessDefinition processDefinition;
    private final State currentState;
    private final Map<String, Object> variables;
    private final String businessId;

    public void setStateProcessResult(Object result) {
        variables.put(ProcessConstant.STATE_PROCESS_RESULT, result);
    }
}