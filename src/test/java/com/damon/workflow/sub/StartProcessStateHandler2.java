package com.damon.workflow.sub;


import com.damon.workflow.RuntimeContext;
import com.damon.workflow.handler.IProcessStateHandler;
import org.springframework.stereotype.Component;


@Component
public class StartProcessStateHandler2 implements IProcessStateHandler {

    @Override
    public void handle(RuntimeContext context) {
        System.out.println(this.getClass().getName() + ":" + context.getVariables());

    }

}
