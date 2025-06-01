package com.damon.workflow.handler;

import com.damon.workflow.RuntimeContext;

/**
 * 流程节点处理器
 */
public interface IProcessStateHandler {
//    default void preHandle(RuntimeContext context) {
//    }

    default void handle(RuntimeContext context) {
    }

    default boolean isMatch(RuntimeContext context) {
        return true;
    }

//    default void postHandle(ComplexProcessResult result, RuntimeContext context) {
//    }
}
