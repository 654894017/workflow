package com.damon.workflow.config;

import com.damon.workflow.ProcessConstant;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.damon.workflow.ProcessConstant.*;


public class State {
    private String id;
    private String type;
    private String name;
    private String nextStateId;
    private List<String> handlers;
    private Map<String, Object> extendParams;
    private List<Condition> conditions;
    private String nextStateCondition;
    private String nextStateConditionScriptType;
    private String nextStateConditionParser;
    private String subProcessIdentifier;
    private boolean endState;

    public boolean isSubProcessState() {
        return ProcessConstant.SUB_PROCESS.equals(type);
    }

    public boolean isTaskState() {
        return Stream.of(START, GENERAL_TASK, END).collect(Collectors.toSet()).contains(type);
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

    public boolean isEndState() {
        return endState;
    }

    public void setEndState(boolean endState) {
        this.endState = endState;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNextStateId() {
        return nextStateId;
    }

    public void setNextStateId(String nextStateId) {
        this.nextStateId = nextStateId;
    }

    public List<String> getHandlers() {
        return handlers;
    }

    public void setHandlers(List<String> handlers) {
        this.handlers = handlers;
    }

    public Map<String, Object> getExtendParams() {
        return extendParams;
    }

    public void setExtendParams(Map<String, Object> extendParams) {
        this.extendParams = extendParams;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public String getNextStateCondition() {
        return nextStateCondition;
    }

    public void setNextStateCondition(String nextStateCondition) {
        this.nextStateCondition = nextStateCondition;
    }

    public String getNextStateConditionScriptType() {
        return nextStateConditionScriptType;
    }

    public void setNextStateConditionScriptType(String nextStateConditionScriptType) {
        this.nextStateConditionScriptType = nextStateConditionScriptType;
    }

    public String getNextStateConditionParser() {
        return nextStateConditionParser;
    }

    public void setNextStateConditionParser(String nextStateConditionParser) {
        this.nextStateConditionParser = nextStateConditionParser;
    }

    public String getSubProcessIdentifier() {
        return subProcessIdentifier;
    }

    public void setSubProcessIdentifier(String subProcessIdentifier) {
        this.subProcessIdentifier = subProcessIdentifier;
    }
}
