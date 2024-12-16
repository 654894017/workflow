package com.damon.workflow.exclusive;

import com.damon.workflow.Application;
import com.damon.workflow.ProcessInstance;
import com.damon.workflow.ProcessResult;
import com.damon.workflow.evaluator.DefaultEvaluator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(classes = Application.class)
public class WorkflowExclusiveGatewayTest {
    @Test
    public void test() {
        ProcessInstance engine = ProcessInstance.loadYaml("flow/WorkflowExclusiveGateway.yaml", DefaultEvaluator.build());
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