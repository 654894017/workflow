package com.damon.workflow.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Condition {
    private String nextStateConditionParser;
    private String condition;
    private String nextState;
    private String scriptType;
}
