package com.damon.workflow.complex.parser;

import com.damon.workflow.RuntimeContext;
import com.damon.workflow.condition_parser.IConditionParser;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ParallelEndConditionParser implements IConditionParser {
    @Override
    public boolean test(RuntimeContext context) {
        Map<String, Object> variables = context.getVariables();
        boolean test = (boolean) variables.get("test");
        return test;
    }
}
