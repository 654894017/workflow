package com.damon.workflow.config;

public class Condition {
    private String nextStateConditionParser;
    private String condition;
    private String nextState;
    private String scriptType;

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getNextState() {
        return nextState;
    }

    public void setNextState(String nextState) {
        this.nextState = nextState;
    }

    public String getScriptType() {
        return scriptType;
    }

    public void setScriptType(String scriptType) {
        this.scriptType = scriptType;
    }

    public String getNextStateConditionParser() {
        return nextStateConditionParser;
    }

    public void setNextStateConditionParser(String nextStateConditionParser) {
        this.nextStateConditionParser = nextStateConditionParser;
    }
}
