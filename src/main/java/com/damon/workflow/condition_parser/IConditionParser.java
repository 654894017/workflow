package com.damon.workflow.condition_parser;

import com.damon.workflow.RuntimeContext;

public interface IConditionParser {
    boolean test(RuntimeContext context);
}
