package com.damon.workflow.exclusive;

import com.damon.workflow.ProcessInstance;
import com.damon.workflow.ProcessResult;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class WorkflowExclusiveGatewayTest2 {
    @Test
    public void test() {
        ProcessInstance engine = ProcessInstance.loadYaml("WorkflowExclusiveGateway2.yaml");
        Map<String, Object> params = new HashMap<>();
        params.put("employeePerformance", 50);

        ProcessResult result = engine.process(params);
        while (result != null) {
            if (!result.isCompleted()) {
                System.out.println(result.getNextStates().get(0).getId());
                result = engine.process(result.getNextStates().get(0).getId(), params);
            } else {
                break;
            }
        }
    }
}