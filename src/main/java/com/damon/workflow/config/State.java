package com.damon.workflow.config;

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

}
