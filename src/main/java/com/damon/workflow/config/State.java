package com.damon.workflow.config;

import com.damon.workflow.ProcessConstant;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.damon.workflow.ProcessConstant.*;

@Getter
@Setter
public class State {
    private String id;
    private String type;
    private String name;
    private String nextStateId;
    private List<String> processors;
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
        return Stream.of(START, USER_TASK, END).collect(Collectors.toSet()).contains(type);
    }

    public boolean isExclusiveGatewayState() {
        return EXCLUSIVE_GATEWAY.equals(type);
    }

    public boolean isParallelStartGatewayState() {
        return PARALLEL_START_GATEWAY.equals(type);
    }

    public boolean isParallelEndGatewayState() {
        return PARALLEL_END_GATEWAY.equals(type);
    }

}
