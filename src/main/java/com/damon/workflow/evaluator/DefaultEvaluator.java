package com.damon.workflow.evaluator;


import com.damon.workflow.RuntimeContext;
import com.damon.workflow.exception.ScriptExecutionException;
import com.damon.workflow.utils.CaseInsensitiveMap;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * eval 执行器
 */
public class DefaultEvaluator implements IEvaluator {
    private final static CaseInsensitiveMap<ScriptEngine> engines = new CaseInsensitiveMap<>();

    private DefaultEvaluator() {
    }

    public static DefaultEvaluator build(String... scriptTypeNames) {
        engines.put("JavaScript", new ScriptEngineManager().getEngineByName("JavaScript"));
        for (String scriptTypeName : scriptTypeNames) {
            engines.put(scriptTypeName, new ScriptEngineManager().getEngineByName(scriptTypeName));
        }
        return new DefaultEvaluator();
    }

    @Override
    public boolean evaluate(String expression, String scriptType, RuntimeContext context) {
        ScriptEngine engine = engines.get(scriptType);
        if (engine == null) {
            throw new IllegalArgumentException("Script engine not found: " + scriptType);
        }
        Bindings bindings = engine.createBindings();
        context.getVariables().forEach(bindings::put);
        try {
            return (Boolean) engine.eval(expression, bindings);
        } catch (ScriptException e) {
            throw new ScriptExecutionException("Condition evaluation failed: " + expression, e);
        }
    }
}
