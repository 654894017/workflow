package com.damon.workflow.config;

public class Condition {
    private String nextStateConditionParser;
    private String condition;
    private String nextStateId;
    private String scriptType;

    public String getNextStateConditionParser() {
        return nextStateConditionParser;
    }

    public void setNextStateConditionParser(String nextStateConditionParser) {
        this.nextStateConditionParser = nextStateConditionParser;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getNextStateId() {
        return nextStateId;
    }

    public void setNextStateId(String nextStateId) {
        this.nextStateId = nextStateId;
    }

    public String getScriptType() {
        return scriptType;
    }

    public void setScriptType(String scriptType) {
        this.scriptType = scriptType;
    }
}
