package com.damon.workflow;

import com.damon.workflow.config.State;
import com.damon.workflow.utils.ClasspathFileUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WorkflowParallelGatewayTest {
    public static void main(String[] args) {
        ProcessEngine engine = new ProcessEngine();
        engine.registerProcessFromCallback("performanceReview", processId -> {
            return ClasspathFileUtils.readFileAsString("WorkflowParallelGateway.yaml");
        });

        Map<String, Object> params = new HashMap<>();
        params.put("employeePerformance", 85);
        params.put("test", false);

        ProcessResult result = engine.process("performanceReview", params);
        System.out.println(result);
        System.out.println("----------------");
        for (State state : result.getNextStates()) {
            System.out.println(state.getId());
        }
        State state1 = new ArrayList<>(result.getNextStates()).get(0);
        Map<String, Object> params2 = new HashMap<>();
        params2.put("test", false);
        ProcessResult result2 = engine.process("performanceReview", state1.getId(), params2);
        System.out.println("----------------");
        for (State state : result2.getNextStates()) {
            System.out.println(state.getId());
        }

        State state2 = new ArrayList<>(result.getNextStates()).get(1);
        Map<String, Object> params3 = new HashMap<>();
        params3.put("test", true);
        ProcessResult result3 = engine.process("performanceReview", state2.getId(), params3);
        System.out.println("----------------");
        System.out.println(result3.isCompleted());
        if (!result3.isCompleted()) {
            for (State state : result3.getNextStates()) {
                System.out.println(state.getId());
            }

        }
    }
}