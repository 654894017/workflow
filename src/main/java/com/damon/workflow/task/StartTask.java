package com.damon.workflow.task;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartTask extends UserTask {

    private final Logger logger = LoggerFactory.getLogger(StartTask.class);

    @Override
    public String getName() {
        return "Start";
    }
}