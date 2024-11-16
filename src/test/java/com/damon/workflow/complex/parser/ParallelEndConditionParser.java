package com.damon.workflow.complex.parser;

import com.damon.workflow.IConditionParser;
import com.damon.workflow.RuntimeContext;

import java.util.Map;

public class ParallelEndConditionParser implements IConditionParser {
    @Override
    public boolean test(RuntimeContext context) {
        Map<String, Object> variables = context.getVariables();
        boolean test = (boolean) variables.get("test");
        return test;
    }
}
