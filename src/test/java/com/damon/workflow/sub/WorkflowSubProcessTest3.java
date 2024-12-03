package com.damon.workflow.sub;


import com.damon.workflow.Application;
import com.damon.workflow.ProcessEngine;
import com.damon.workflow.ProcessInstance;
import com.damon.workflow.ProcessResult3;
import com.damon.workflow.config.StateIdentifier;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;

@SpringBootTest(classes = Application.class)
public class WorkflowSubProcessTest3 {
    @Test
    public void test() {
        ProcessInstance processInstance = ProcessInstance.loadYaml("WorkflowMain.yaml");
        ProcessEngine engine = new ProcessEngine();
        engine.registerProcessInstance("WorkflowSub.yaml");
        engine.registerProcessInstance(processInstance);

        ProcessResult3 result = engine.process(
                new StateIdentifier("performanceReview:1.0", "Start"), new HashMap<>(), "1"
        );

        System.out.println(1);

        ProcessResult3 result2 = engine.process(
                new StateIdentifier("performanceReview:1.0", "SubProcess1", "SubProcess2:1.0", "Start"),
                new HashMap<>(), "1"
        );

        System.out.println(1);


        ProcessResult3 result3 = engine.process(
                new StateIdentifier("performanceReview:1.0", "SubProcess1", "SubProcess2:1.0", "aaa"),
                new HashMap<>(), "1"
        );

        System.out.println(1);


    }
}