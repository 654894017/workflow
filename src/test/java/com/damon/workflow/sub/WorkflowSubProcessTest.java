package com.damon.workflow.sub;


import com.damon.workflow.Application;
import com.damon.workflow.ProcessEngine;
import com.damon.workflow.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = Application.class)
public class WorkflowSubProcessTest {
    @Test
    public void test() {
        ProcessInstance processInstance = ProcessInstance.loadYaml("WorkflowMain.yaml");
        ProcessEngine engine = new ProcessEngine();
        engine.registerProcessInstance("WorkflowSub.yaml");
        engine.registerProcessInstance(processInstance);

//        ProcessResult2 result = engine.process2(
//                new StateIdentifier("performanceReview:1.0", "performanceReview:1.0", "Start"), new HashMap<>(), "1"
//        );
//
//        ProcessResult2 result2 = engine.process2(
//                new StateIdentifier("performanceReview:1.0", "SubProcess2:1.0", "Start"),
//                new HashMap<>(), "1"
//        );
//
//        ProcessResult2 result3 = engine.process2(
//                new StateIdentifier("performanceReview:1.0", "SubProcess2:1.0", "aaa"),
//                new HashMap<>(), "1"
//        );
//

        System.out.println(1);


    }
}