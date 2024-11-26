package com.damon.workflow.task;


import com.damon.workflow.evaluator.IEvaluator;
import com.damon.workflow.utils.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartTask extends UserTask {

    private final Logger logger = LoggerFactory.getLogger(StartTask.class);

    public StartTask(CaseInsensitiveMap<IEvaluator> evaluatorMap) {
        super(evaluatorMap);
    }

    @Override
    public String getName() {
        return "Start";
    }
}