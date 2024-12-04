package com.damon.workflow.sub;


import com.damon.workflow.Application;
import com.damon.workflow.ComplexProcessResult;
import com.damon.workflow.ProcessEngine;
import com.damon.workflow.config.StateIdentifier;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

@SpringBootTest(classes = Application.class)
public class WorkflowSubProcessTest {
    @Test
    public void test() {
        ProcessEngine engine = new ProcessEngine();
        String processIdentifier = engine.registerProcessInstance("WorkflowMain.yaml");
        engine.registerProcessInstance("WorkflowSub.yaml");

        ComplexProcessResult result = engine.process(processIdentifier, new HashMap<>());

        result.getNextStates().forEach(state -> {
            System.out.println(state.getNextStateFullPaths());
        });

        ComplexProcessResult result2 = engine.process(
                StateIdentifier.build(result.getNextStates().get(0).getNextStateFullPaths()), new HashMap<>()
        );

        result2.getNextStates().forEach(state -> {
            System.out.println(state.getNextStateFullPaths());
        });


        ComplexProcessResult result3 = engine.process(
                StateIdentifier.build(result2.getNextStates().get(0).getNextStateFullPaths()), new HashMap<>()
        );

        result3.getNextStates().forEach(state -> {
            System.out.println(state.getNextStateFullPaths());
        });


        ComplexProcessResult result4 = engine.process(
                StateIdentifier.build(result3.getNextStates().get(0).getNextStateFullPaths()), new HashMap<>()
        );

        result4.getNextStates().forEach(state -> {
            System.out.println(state.getNextStateFullPaths());
        });

    }
}