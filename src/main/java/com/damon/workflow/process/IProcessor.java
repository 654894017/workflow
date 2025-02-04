package com.damon.workflow.process;

import com.damon.workflow.ComplexProcessResult;
import com.damon.workflow.RuntimeContext;

public interface IProcessor {
    default void preProcess(RuntimeContext context) {
    }

    default void process(RuntimeContext context) {
    }

    default boolean isMatch(RuntimeContext context) {
        return true;
    }

    default void postProcess(ComplexProcessResult result, RuntimeContext context) {
    }
}
