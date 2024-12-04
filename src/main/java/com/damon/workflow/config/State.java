package com.damon.workflow.config;

import com.damon.workflow.ProcessConstant;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class State {
    private String id;
    private String type;
    private String name;
    private String nextStateId;
    private String processor;
    private Map<String, Object> extendParams;
    private List<Condition> conditions;
    private String nextStateCondition;
    private String nextStateConditionScriptType;
    private String nextStateConditionParser;
    private String subProcessIdentifier;

    public boolean isSubProcessState() {
        return ProcessConstant.SUB_PROCESS.equals(type);
    }

    public boolean isTaskState() {
        return ProcessConstant.isTaskState(type);
    }

    public boolean isExclusiveGatewayState() {
        return ProcessConstant.EXCLUSIVE_GATEWAY.equals(type);
    }

    public boolean isParallelStartGatewayState() {
        return ProcessConstant.PARALLEL_START_GATEWAY.equals(type);
    }

    public boolean isParallelEndGatewayState() {
        return ProcessConstant.PARALLEL_END_GATEWAY.equals(type);
    }

}
