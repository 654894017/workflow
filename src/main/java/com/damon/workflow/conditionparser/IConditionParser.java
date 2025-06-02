package com.damon.workflow.conditionparser;

import com.damon.workflow.RuntimeContext;

public interface IConditionParser {
    boolean test(RuntimeContext context);
}
