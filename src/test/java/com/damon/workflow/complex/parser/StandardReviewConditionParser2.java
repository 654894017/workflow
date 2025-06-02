package com.damon.workflow.complex.parser;

import com.damon.workflow.RuntimeContext;
import com.damon.workflow.conditionparser.IConditionParser;
import org.springframework.stereotype.Component;

@Component
public class StandardReviewConditionParser2 implements IConditionParser {
    @Override
    public boolean test(RuntimeContext context) {
        return true;
    }
}
