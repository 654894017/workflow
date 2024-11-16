package com.damon.workflow.exclusive;

import com.damon.workflow.ProcessEngine;
import com.damon.workflow.ProcessResult;
import com.damon.workflow.exclusive.parser.HighPerformanceReviewConditionParser;
import com.damon.workflow.exclusive.parser.StandardReviewConditionParser;
import com.damon.workflow.exclusive.processor.HighPerformanceReviewProcessor;
import com.damon.workflow.exclusive.processor.StandardReviewProcessor;
import com.damon.workflow.exclusive.processor.StartProcessor;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class WorkflowExclusiveGatewayTest {
    private final static String PROCESS_ID = "performanceReview";

    @Test
    public void test() {
        ProcessEngine engine = new ProcessEngine();
        engine.registerProcessFromClasspath(PROCESS_ID, "WorkflowExclusiveGateway.yaml");
        engine.registerProcessors(PROCESS_ID,
                new StartProcessor(),
                new HighPerformanceReviewProcessor(),
                new StandardReviewProcessor()
        );
        engine.registerConditionParsers(PROCESS_ID, new HighPerformanceReviewConditionParser());
        engine.registerConditionParsers(PROCESS_ID, new StandardReviewConditionParser());
        Map<String, Object> params = new HashMap<>();
        params.put("employeePerformance", 85);

        ProcessResult result = engine.process(PROCESS_ID, params);
        System.out.println((Object) result.getResult());
        while (result != null) {
            if (!result.isCompleted()) {
                result = engine.process(PROCESS_ID, result.getNextStates().get(0).getId(), params);
                System.out.println((Object) result.getResult());
            } else {
                break;
            }
        }
    }
}