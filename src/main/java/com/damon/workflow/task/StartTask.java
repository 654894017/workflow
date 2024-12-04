package com.damon.workflow.task;


import com.damon.workflow.evaluator.IEvaluator;
import com.damon.workflow.utils.CaseInsensitiveMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StartTask extends UserTask {

    public StartTask(CaseInsensitiveMap<IEvaluator> evaluatorMap) {
        super(evaluatorMap);
    }

    @Override
    public String getName() {
        return "Start";
    }
}