package com.damon.workflow.exclusive.parser;

import com.damon.workflow.RuntimeContext;
import com.damon.workflow.conditionparser.IConditionParser;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StandardReviewConditionParser implements IConditionParser {
    @Override
    public boolean test(RuntimeContext context) {
        Map<String, Object> variables = context.getVariables();
        int score = (int) variables.get("employeePerformance");
        return score <= 85;
    }
}
