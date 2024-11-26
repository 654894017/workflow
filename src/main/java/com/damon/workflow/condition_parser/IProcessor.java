package com.damon.workflow.condition_parser;

import com.damon.workflow.RuntimeContext;

public interface IProcessor {
    void process(RuntimeContext context);
}
