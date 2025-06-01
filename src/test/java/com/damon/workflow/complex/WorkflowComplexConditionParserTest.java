package com.damon.workflow.complex;

import com.damon.workflow.Application;
import com.damon.workflow.ComplexProcessResult;
import com.damon.workflow.ProcessInstance;
import com.damon.workflow.config.NextState;
import com.damon.workflow.config.StateIdentifier;
import com.damon.workflow.engine.ProcessEngine;
import com.damon.workflow.evaluator.DefaultEvaluator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(classes = Application.class)
public class WorkflowComplexConditionParserTest {
    @Test
    public void test() {
        ProcessEngine engine = new ProcessEngine.Builder().evaluator(DefaultEvaluator.build()).build();
        ProcessInstance instance = engine.getProcessInstance("WorkflowComplexConditionParser:1.0");
        System.out.println(instance.getProcessDefinition().getStateInverseRelation());
        ComplexProcessResult result = engine.process("WorkflowComplexConditionParser:1.0", new HashMap<>(), "1");
        System.out.println("----------------");
        for (NextState nextState : result.getNextStates()) {
            System.out.println(nextState.getNextStateFullPaths());
        }
        Map<String, Object> params2 = new HashMap<>();
        params2.put("employeePerformance", 60);
        ComplexProcessResult result2 = engine.process(StateIdentifier.buildFromFullPaths(result.getNextStates().get(0).getNextStateFullPaths()), params2, "1");
        System.out.println("----------------");
        for (NextState nextState : result2.getNextStates()) {
            System.out.println(nextState.getNextStateFullPaths());
        }

        ComplexProcessResult result3 = engine.process(StateIdentifier.buildFromFullPaths(result2.getNextStates().get(0).getNextStateFullPaths()), params2, "1");
        System.out.println("----------------");
        for (NextState nextState : result3.getNextStates()) {
            System.out.println(nextState.getNextStateFullPaths());
        }

        ComplexProcessResult result4 = engine.process(StateIdentifier.buildFromFullPaths(result3.getNextStates().get(0).getNextStateFullPaths()), params2, "1");
        System.out.println("----------------");
        for (NextState nextState : result4.getNextStates()) {
            System.out.println(nextState.getNextStateFullPaths());
        }

        HashMap params4 = new HashMap<>();
        params4.put("employeePerformance", 90);
        ComplexProcessResult result5 = engine.process(StateIdentifier.buildFromFullPaths(result4.getNextStates().get(0).getNextStateFullPaths()), params4, "1");
        System.out.println("----------------");
        for (NextState nextState : result5.getNextStates()) {
            System.out.println(nextState.getNextStateFullPaths());
        }

        HashMap params5 = new HashMap<>();
        params5.put("employeePerformance", 50);
        ComplexProcessResult result6 = engine.process(StateIdentifier.buildFromFullPaths(result4.getNextStates().get(1).getNextStateFullPaths()), params5, "1");
        System.out.println("----------------");
        for (NextState nextState : result6.getNextStates()) {
            System.out.println(nextState.getNextStateFullPaths());
        }

        System.out.println(result6.isCompleted());

    }
}