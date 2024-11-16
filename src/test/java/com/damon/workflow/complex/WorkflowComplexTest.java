package com.damon.workflow.complex;

import com.damon.workflow.ProcessEngine;
import com.damon.workflow.ProcessResult;
import com.damon.workflow.config.State;
import com.damon.workflow.utils.ClasspathFileUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WorkflowComplexTest {
    @Test
    public void test() {
        ProcessEngine engine = new ProcessEngine(ClasspathFileUtils.readFileAsString("WorkflowComplex.yaml"));

        ProcessResult result = engine.process(new HashMap<>());
        System.out.println("----------------");
        for (State state : result.getNextStates()) {
            System.out.println(state.getId());
        }
        State state1 = new ArrayList<>(result.getNextStates()).get(0);
        Map<String, Object> params2 = new HashMap<>();
        params2.put("a", 60);
        ProcessResult result2 = engine.process(state1.getId(), params2);
        System.out.println("----------------");
        for (State state : result2.getNextStates()) {
            System.out.println(state.getId());
        }

        State state2 = new ArrayList<>(result2.getNextStates()).get(0);
        ProcessResult result3 = engine.process(state2.getId(), params2);
        System.out.println("----------------");
        for (State state : result3.getNextStates()) {
            System.out.println(state.getId());
        }


        State state3 = new ArrayList<>(result3.getNextStates()).get(0);
        ProcessResult result4 = engine.process(state3.getId(), new HashMap<>());
        System.out.println("----------------");
        for (State state : result4.getNextStates()) {
            System.out.println(state.getId());
        }


        State state4 = new ArrayList<>(result4.getNextStates()).get(0);
        State state5 = new ArrayList<>(result4.getNextStates()).get(1);

        HashMap params4 = new HashMap<>();
        params4.put("test", false);
        ProcessResult result5 = engine.process(state4.getId(), params4);
        System.out.println("----------------");
        for (State state : result5.getNextStates()) {
            System.out.println(state.getId());
        }
        HashMap params5 = new HashMap<>();
        params5.put("test", true);
        ProcessResult result6 = engine.process(state5.getId(), params5);
        for (State state : result6.getNextStates()) {
            System.out.println(state.getId());
        }

    }
}