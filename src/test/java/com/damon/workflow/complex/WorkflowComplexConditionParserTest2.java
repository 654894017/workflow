package com.damon.workflow.complex;

import com.damon.workflow.Application;
import com.damon.workflow.ComplexProcessResult;
import com.damon.workflow.engine.LongTransactionProcessEngine;
import com.damon.workflow.engine.ProcessEngine;
import com.damon.workflow.evaluator.DefaultEvaluator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.concurrent.Executors;

@SpringBootTest(classes = Application.class)
public class WorkflowComplexConditionParserTest2 {
    @Test
    public void test() {
        ProcessEngine engine = new ProcessEngine.Builder()
                .executorService(Executors.newVirtualThreadPerTaskExecutor())
                .evaluator(DefaultEvaluator.build()).build();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            ComplexProcessResult result = engine.trigger("WorkflowTest:1.0", new HashMap<>());
            System.out.println(result);
            //新增流转记录
        }
        System.out.println("耗时：" + (System.currentTimeMillis() - startTime));

    }


    @Test
    public void test2() {
        LongTransactionProcessEngine engine = new LongTransactionProcessEngine.Builder()
                .executorService(Executors.newVirtualThreadPerTaskExecutor())
                .evaluator(DefaultEvaluator.build()).build();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            ComplexProcessResult result = engine.process("WorkflowTest:1.0", new HashMap<>());
            System.out.println(result);
        }
        System.out.println("耗时：" + (System.currentTimeMillis() - startTime));

    }
}