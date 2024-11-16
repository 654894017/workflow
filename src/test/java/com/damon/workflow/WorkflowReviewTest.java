package com.damon.workflow;

import com.damon.workflow.config.State;
import com.damon.workflow.utils.ClasspathFileUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WorkflowReviewTest {
    @Test
    public void test() {
        ProcessEngine engine = new ProcessEngine();
        engine.registerProcessFromCallback("performanceReview", processId -> {
            return ClasspathFileUtils.readFileAsString("WorkflowReview.yaml");
        });
        HashMap params1 = new HashMap<>();
        params1.put("a", 20);
        ProcessResult result = engine.process("performanceReview", params1);
        System.out.println("----------------");
        for (State state : result.getNextStates()) {
            System.out.println(state.getId());
        }
        State state1 = new ArrayList<>(result.getNextStates()).get(0);
        Map<String, Object> params2 = new HashMap<>();
        params2.put("a", 60);
        ProcessResult result2 = engine.process("performanceReview", state1.getId(), params2);
        System.out.println("----------------");
        for (State state : result2.getNextStates()) {
            System.out.println(state.getId());
        }


    }
}