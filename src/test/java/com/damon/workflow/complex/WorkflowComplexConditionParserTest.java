package com.damon.workflow.complex;

import com.damon.workflow.ProcessEngine;
import com.damon.workflow.ProcessResult;
import com.damon.workflow.complex.parser.HighPerformanceReviewConditionParser;
import com.damon.workflow.complex.parser.ParallelEndConditionParser;
import com.damon.workflow.complex.parser.StandardReviewConditionParser;
import com.damon.workflow.complex.processor.*;
import com.damon.workflow.config.State;
import com.damon.workflow.utils.ClasspathFileUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WorkflowComplexConditionParserTest {
    @Test
    public void test() {
        ProcessEngine engine = new ProcessEngine();
        String PROCESS_ID = "performanceReview";
        engine.registerProcessFromCallback("performanceReview", processId -> {
            return ClasspathFileUtils.readFileAsString("WorkflowComplexConditionParser.yaml");
        });
        engine.registerProcessors(PROCESS_ID,
                new StartProcessor(),
                new HighPerformanceReviewProcessor(),
                new StandardReviewProcessor(),
                new UserTask1Processor(),
                new UserTask2Processor(),
                new UserTask3Processor(),
                new UserTask4Processor()

        );
        engine.registerConditionParsers(PROCESS_ID,
                new HighPerformanceReviewConditionParser(),
                new StandardReviewConditionParser(),
                new ParallelEndConditionParser()
        );

        ProcessResult result = engine.process("performanceReview", new HashMap<>());
        System.out.println("----------------");
        for (State state : result.getNextStates()) {
            System.out.println(state.getId());
        }
        State state1 = new ArrayList<>(result.getNextStates()).get(0);
        Map<String, Object> params2 = new HashMap<>();
        params2.put("employeePerformance", 60);
        ProcessResult result2 = engine.process("performanceReview", state1.getId(), params2);
        System.out.println("----------------");
        for (State state : result2.getNextStates()) {
            System.out.println(state.getId());
        }

        State state2 = new ArrayList<>(result2.getNextStates()).get(0);
        ProcessResult result3 = engine.process("performanceReview", state2.getId(), params2);
        System.out.println("----------------");
        for (State state : result3.getNextStates()) {
            System.out.println(state.getId());
        }


        State state3 = new ArrayList<>(result3.getNextStates()).get(0);
        ProcessResult result4 = engine.process("performanceReview", state3.getId(), new HashMap<>());
        System.out.println("----------------");
        for (State state : result4.getNextStates()) {
            System.out.println(state.getId());
        }


        State state4 = new ArrayList<>(result4.getNextStates()).get(0);
        State state5 = new ArrayList<>(result4.getNextStates()).get(1);

        HashMap params4 = new HashMap<>();
        params4.put("employeePerformance", 90);
        ProcessResult result5 = engine.process("performanceReview", state4.getId(), params4);
        System.out.println("----------------");
        for (State state : result5.getNextStates()) {
            System.out.println(state.getId());
        }
        HashMap params5 = new HashMap<>();
        params5.put("employeePerformance", 50);
        ProcessResult result6 = engine.process("performanceReview", state5.getId(), params5);
        for (State state : result6.getNextStates()) {
            System.out.println(state.getId());
        }

    }
}