package com.damon.workflow.complex.processor;

import com.damon.workflow.RuntimeContext;
import com.damon.workflow.handler.IProcessStateHandler;
import org.springframework.stereotype.Component;

@Component
public class UserTask11StateHandler implements IProcessStateHandler {
    @Override
    public void handle(RuntimeContext context) {
        System.out.println(context.getCurrentStateIdentifier().getFullPaths() + ":" + context.getVariables());
        context.setResult(context.getCurrentStateIdentifier().getFullPaths());
    }

}
