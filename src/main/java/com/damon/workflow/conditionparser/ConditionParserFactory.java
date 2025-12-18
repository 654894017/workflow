package com.damon.workflow.conditionparser;

import com.damon.workflow.spring.ApplicationContextHelper;

public class ConditionParserFactory {

    public static IConditionParser getConditionParser(String conditionParserType) {
        return ApplicationContextHelper.getBean(conditionParserType);
    }


}
