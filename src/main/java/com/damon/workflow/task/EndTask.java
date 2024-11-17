package com.damon.workflow.task;


import com.damon.workflow.IProcessor;
import com.damon.workflow.utils.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndTask extends UserTask {

    private final Logger logger = LoggerFactory.getLogger(EndTask.class);

    public EndTask(CaseInsensitiveMap<IProcessor> processorsMap) {
        super(processorsMap);
    }

    @Override
    public String getName() {
        return "End";
    }


}