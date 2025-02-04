package com.damon.workflow.sub;


import com.damon.workflow.Application;
import com.damon.workflow.ComplexProcessResult;
import com.damon.workflow.config.StateIdentifier;
import com.damon.workflow.engine.LongTransactionProcessEngine;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

@SpringBootTest(classes = Application.class)
public class WorkflowSubProcessTest {
    @Test
    public void test() {
        LongTransactionProcessEngine engine = new LongTransactionProcessEngine.Builder().build();
        ComplexProcessResult result = engine.process("WorkflowMain:1.0", new HashMap<>());

        result.getNextStates().forEach(state -> {
            System.out.println(state.getNextStateFullPaths());
        });

        String stateProcessResult = result.getStateProcessResult();
        System.out.println(stateProcessResult);

        ComplexProcessResult result2 = engine.process(
                StateIdentifier.buildFromFullPaths(result.getNextStates().get(0).getNextStateFullPaths()), new HashMap<>()
        );

        result2.getNextStates().forEach(state -> {
            System.out.println(state.getNextStateFullPaths());
        });

        ComplexProcessResult result3 = engine.process(
                StateIdentifier.buildFromFullPaths(result2.getNextStates().get(0).getNextStateFullPaths()), new HashMap<>()
        );

        result3.getNextStates().forEach(state -> {
            System.out.println(state.getNextStateFullPaths());
        });

        ComplexProcessResult result4 = engine.process(
                StateIdentifier.buildFromFullPaths(result3.getNextStates().get(0).getNextStateFullPaths()), new HashMap<>()
        );

        result4.getNextStates().forEach(state -> {
            System.out.println(state.getNextStateFullPaths());
        });

    }
}