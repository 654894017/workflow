package com.damon.workflow.complex.processor;

import com.damon.workflow.RuntimeContext;
import com.damon.workflow.handler.IProcessStateHandler;
import org.springframework.stereotype.Component;

@Component
public class StartStateHandler implements IProcessStateHandler {
    @Override
    public void handle(RuntimeContext context) {
        System.out.println(context.getCurrentStateIdentifier().getFullPaths() + ":" + context.getVariables());
    }

}
