package com.damon.workflow.evaluator;


import com.damon.workflow.RuntimeContext;
import com.damon.workflow.exception.ScriptExecutionException;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class JavaScriptEvaluator implements IEvaluator {
    private ScriptEngine engine = new ScriptEngineManager().getEngineByName(getName());

    @Override
    public boolean evaluate(String expression, RuntimeContext context) {
        Bindings bindings = engine.createBindings();
        context.getVariables().forEach((field, value) -> {
            bindings.put(field, value);
        });
        try {
            return (Boolean) engine.eval(expression, bindings);
        } catch (ScriptException e) {
            throw new ScriptExecutionException("Condition evaluation failed: " + expression, e);
        }
    }

    @Override
    public String getName() {
        return "JavaScript";
    }
}
