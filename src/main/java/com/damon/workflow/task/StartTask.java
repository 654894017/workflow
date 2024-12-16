package com.damon.workflow.task;


import com.damon.workflow.evaluator.IEvaluator;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StartTask extends UserTask {

    public StartTask(IEvaluator evaluator) {
        super(evaluator);
    }

    @Override
    public String getName() {
        return "Start";
    }
}