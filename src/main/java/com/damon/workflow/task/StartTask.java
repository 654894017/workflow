package com.damon.workflow.task;


import com.damon.workflow.IProcessor;
import com.damon.workflow.utils.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartTask extends UserTask {

    private final Logger logger = LoggerFactory.getLogger(StartTask.class);

    public StartTask(CaseInsensitiveMap<IProcessor> processorsMap) {
        super(processorsMap);
    }

    @Override
    public String getName() {
        return "Start";
    }
}