package com.damon.workflow.sub;

import com.damon.workflow.RuntimeContext;
import com.damon.workflow.condition_parser.IProcessor;
import org.springframework.stereotype.Component;


@Component
public class StartProcessor4 implements IProcessor {

    @Override
    public void process(RuntimeContext context) {
        System.out.println(this.getClass().getName() + ":" + context.getVariables());

    }

}