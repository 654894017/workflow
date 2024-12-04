package com.damon.workflow.sub;


import com.damon.workflow.Application;
import com.damon.workflow.ComplexProcessResult;
import com.damon.workflow.ProcessEngine;
import com.damon.workflow.ProcessInstance;
import com.damon.workflow.config.StateIdentifier;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

@SpringBootTest(classes = Application.class)
public class WorkflowSubProcessTest {
    @Test
    public void test() {
        ProcessInstance processInstance = ProcessInstance.loadYaml("WorkflowMain.yaml");
        ProcessEngine engine = new ProcessEngine();
        engine.registerProcessInstance("WorkflowSub.yaml");
        engine.registerProcessInstance(processInstance);

        ComplexProcessResult result = engine.process(
                StateIdentifier.buildByStateIdentifiers("performanceReview:1.0", "Start"), new HashMap<>(), "1"
        );

        result.getNextStates().forEach(state -> {
            System.out.println(state.getNextStateFullPaths());
        });

        ComplexProcessResult result2 = engine.process(
                StateIdentifier.buildByStateIdentifiers("performanceReview:1.0", "SubProcess1", "SubProcess2:1.0", "Start"),
                new HashMap<>(), "1"
        );

        result2.getNextStates().forEach(state -> {
            System.out.println(state.getNextStateFullPaths());
        });


        ComplexProcessResult result3 = engine.process(
                StateIdentifier.buildByStateIdentifiers("performanceReview:1.0", "SubProcess1", "SubProcess2:1.0", "aaa"),
                new HashMap<>(), "1"
        );

        result3.getNextStates().forEach(state -> {
            System.out.println(state.getNextStateFullPaths());
        });


        ComplexProcessResult result4 = engine.process(
                StateIdentifier.buildByStateIdentifiers("performanceReview:1.0", "StandardReview"),
                new HashMap<>(), "1"
        );

        result4.getNextStates().forEach(state -> {
            System.out.println(state.getNextStateFullPaths());
        });

    }
}