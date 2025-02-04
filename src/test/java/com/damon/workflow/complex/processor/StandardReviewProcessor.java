package com.damon.workflow.complex.processor;

import com.damon.workflow.RuntimeContext;
import com.damon.workflow.process.IProcessor;
import org.springframework.stereotype.Component;

@Component
public class StandardReviewProcessor implements IProcessor {
    @Override
    public void process(RuntimeContext context) {
        System.out.println(context.getCurrentStateIdentifier().getFullPaths() + ":" + context.getVariables());
    }

}
