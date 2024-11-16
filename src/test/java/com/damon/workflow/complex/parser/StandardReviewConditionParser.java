package com.damon.workflow.complex.parser;

import com.damon.workflow.IConditionParser;
import com.damon.workflow.RuntimeContext;

import java.util.Map;

public class StandardReviewConditionParser implements IConditionParser {
    @Override
    public boolean test(RuntimeContext context) {
        Map<String, Object> variables = context.getVariables();
        int score = (int) variables.get("employeePerformance");
        return score <= 85;
    }
}
