package com.damon.workflow.evaluator;


import com.damon.workflow.RuntimeContext;
import com.damon.workflow.exception.ScriptExecutionException;
import org.springframework.stereotype.Component;

import javax.script.*;

@Component
public class GroovyEvaluator implements IEvaluator {

    private ScriptEngine engine = new ScriptEngineManager().getEngineByName(getName());

    public static void main(String[] args) {
        //test1();

        test2();


    }

    public static void test1() {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("groovy");

        try {
            CompiledScript script = ((Compilable) engine).compile("1+2+3+4+5");
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000000; i++) {
                script.eval();
            }
            System.out.println("cost:" + (System.currentTimeMillis() - start));
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }

    }

    public static void test2() {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("groovy");

        try {
            long start = System.currentTimeMillis();
            for (int i = 0; i < 1000000; i++) {
                engine.eval("1+2+3+4+5");
            }
            System.out.println("cost:" + (System.currentTimeMillis() - start));
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean evaluate(String expression, RuntimeContext context) {
        Bindings bindings = engine.createBindings();
        context.getVariables().forEach(bindings::put);
        try {
            return (Boolean) engine.eval(expression, bindings);
        } catch (ScriptException e) {
            throw new ScriptExecutionException("Condition evaluation failed: " + expression, e);
        }
    }

    @Override
    public String getName() {
        return "Groovy";
    }

}
