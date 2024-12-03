package com.damon.workflow.complex.processor;

import com.damon.workflow.RuntimeContext;
import com.damon.workflow.condition_parser.IProcessor;
import org.springframework.stereotype.Component;

@Component
public class UserTask3Processor implements IProcessor {
    @Override
    public void process(RuntimeContext context) {
        System.out.println(context.getCurrentState().getId() + ":" + context.getVariables());
    }


}
