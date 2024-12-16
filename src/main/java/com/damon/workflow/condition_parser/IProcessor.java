package com.damon.workflow.condition_parser;

import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.State;

import java.util.Map;

public interface IProcessor {
    void process(RuntimeContext context);
    default boolean isMatch(State currentState, Map<String, Object> variables) {
        return true;
    }
}
