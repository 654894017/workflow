package com.damon.workflow.task.impl;


import com.damon.workflow.ProcessConstant;
import com.damon.workflow.evaluator.IEvaluator;

public class StartTask extends GeneralTask {

    public StartTask(IEvaluator evaluator) {
        super(evaluator);
    }

    @Override
    public String getName() {
        return ProcessConstant.START;
    }
}