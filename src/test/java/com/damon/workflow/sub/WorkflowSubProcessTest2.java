package com.damon.workflow.sub;


import com.damon.workflow.Application;
import com.damon.workflow.ProcessEngine;
import com.damon.workflow.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = Application.class)
public class WorkflowSubProcessTest2 {
    @Test
    public void test() {
        ProcessInstance processInstance = ProcessInstance.loadYaml("WorkflowMain.yaml");
        ProcessEngine engine = new ProcessEngine();
        engine.registerProcessInstance("WorkflowSub.yaml");
        engine.registerProcessInstance(processInstance);

//        ProcessResult3 result = engine.process3(
//                new StateIdentifier2("performanceReview:1.0", "Start"), new HashMap<>(), "1"
//        );
//
//        System.out.println(1);
//
//        ProcessResult3 result2 = engine.process3(
//                new StateIdentifier2(result.getNextStates().get(0).getNextStateProcessFullpath(), "Start"),
//                new HashMap<>(), "1"
//        );
//
//        System.out.println(1);
//
//
//        ProcessResult3 result3 = engine.process3(
//                new StateIdentifier2(result2.getNextStates().get(0).getNextStateProcessFullpath(), "aaa"),
//                new HashMap<>(), "1"
//        );


        System.out.println(1);


    }
}