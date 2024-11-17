package com.damon.workflow.config;

import java.util.List;
import java.util.Objects;

public class State {
    private String id;
    private String type;
    private String name;
    private String assigneeRole;
    private String assigneeUser;
    private String requiredStatus;
    private ExtendInformation extendInformation;
    private List<Condition> conditions;
    private String nextState;
    private String nextStateCondition;
    private String nextStateConditionScriptType;
    private String nextStateConditionParser;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAssigneeRole() {
        return assigneeRole;
    }

    public void setAssigneeRole(String assigneeRole) {
        this.assigneeRole = assigneeRole;
    }

    public String getRequiredStatus() {
        return requiredStatus;
    }

    public void setRequiredStatus(String requiredStatus) {
        this.requiredStatus = requiredStatus;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public String getNextState() {
        return nextState;
    }

    public void setNextState(String nextState) {
        this.nextState = nextState;
    }

    public String getAssigneeUser() {
        return assigneeUser;
    }

    public void setAssigneeUser(String assigneeUser) {
        this.assigneeUser = assigneeUser;
    }

    public ExtendInformation getExtendInformation() {
        return extendInformation;
    }

    public void setExtendInformation(ExtendInformation extendInformation) {
        this.extendInformation = extendInformation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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
