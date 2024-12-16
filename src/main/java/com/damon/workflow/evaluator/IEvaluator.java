package com.damon.workflow.evaluator;


import com.damon.workflow.RuntimeContext;

public interface IEvaluator {

    boolean evaluate(String expression, String scriptType, RuntimeContext context);

}
