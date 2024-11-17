package com.damon.workflow.complex.processor;

import com.damon.workflow.IProcessor;
import com.damon.workflow.RuntimeContext;
import com.damon.workflow.utils.Sets;

import java.util.Set;

public class UserTask1Processor implements IProcessor {
    @Override
    public void process(RuntimeContext context) {
        System.out.println(context.getCurrentState().getId() + ":" + context.getVariables());
    }

    @Override
    public Set<String> stateIds() {
        return Sets.newHashSet("UserTask1");
    }
}
