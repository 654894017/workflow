package com.damon.workflow.exclusive;

import com.damon.workflow.ProcessEngine;
import com.damon.workflow.ProcessResult;
import com.damon.workflow.exclusive.parser.HighPerformanceReviewConditionParser;
import com.damon.workflow.exclusive.parser.StandardReviewConditionParser;
import com.damon.workflow.exclusive.processor.HighPerformanceReviewProcessor;
import com.damon.workflow.exclusive.processor.StandardReviewProcessor;
import com.damon.workflow.exclusive.processor.StartProcessor;
import com.damon.workflow.utils.ClasspathFileUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class WorkflowExclusiveGatewayTest {
    @Test
    public void test() {
        ProcessEngine engine = new ProcessEngine(ClasspathFileUtils.readFileAsString("WorkflowExclusiveGateway.yaml"));
        engine.registerProcessors(
                new StartProcessor(),
                new HighPerformanceReviewProcessor(),
                new StandardReviewProcessor()
        );
        engine.registerConditionParsers(new HighPerformanceReviewConditionParser());
        engine.registerConditionParsers(new StandardReviewConditionParser());
        Map<String, Object> params = new HashMap<>();
        params.put("employeePerformance", 85);

        ProcessResult result = engine.process(params);
        while (result != null) {
            if (!result.isCompleted()) {
                result = engine.process(result.getNextStates().get(0).getId(), params);
            } else {
                break;
            }
        }
    }
}