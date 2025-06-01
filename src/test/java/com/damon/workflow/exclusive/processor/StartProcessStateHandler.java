package com.damon.workflow.exclusive.processor;

import com.damon.workflow.RuntimeContext;
import com.damon.workflow.handler.IProcessStateHandler;
import org.springframework.stereotype.Component;

@Component
public class StartProcessStateHandler implements IProcessStateHandler {
    @Override
    public void handle(RuntimeContext context) {
        System.out.println(context.getCurrentStateIdentifier().getFullPaths() + ":" + context.getVariables());
    }
}
