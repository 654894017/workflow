package com.damon.workflow;

import java.util.HashMap;
import java.util.Map;

public class WorkflowExclusiveGatewayTest {
    private final static String PROCESS_ID = "performanceReview";

    public static void main(String[] args) {
        ProcessEngine engine = new ProcessEngine();
        engine.registerProcessFromClasspath(PROCESS_ID, "WorkflowExclusiveGateway.yaml");

        Map<String, Object> params = new HashMap<>();
        params.put("employeePerformance", 85);

        ProcessResult result = engine.process(PROCESS_ID, params);
        while (result != null) {
            if (!result.isCompleted()) {
                result = engine.process(PROCESS_ID, result.getNextStates().get(0).getId(), params);
            } else {
                break;
            }
        }
    }
}