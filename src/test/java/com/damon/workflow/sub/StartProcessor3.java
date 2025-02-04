package com.damon.workflow.sub;

import com.damon.workflow.RuntimeContext;
import com.damon.workflow.process.IProcessor;
import org.springframework.stereotype.Component;


@Component
public class StartProcessor3 implements IProcessor {

    @Override
    public void process(RuntimeContext context) {
        System.out.println(this.getClass().getName() + ":" + context.getVariables());

    }

}
