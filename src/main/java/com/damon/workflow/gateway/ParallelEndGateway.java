package com.damon.workflow.gateway;

import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.evaluator.IEvaluator;
import com.damon.workflow.utils.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class ParallelEndGateway implements IGateway {

    private final Logger logger = LoggerFactory.getLogger(ParallelStartGateway.class);

    private final CaseInsensitiveMap<IEvaluator> evaluatorMap;

    public ParallelEndGateway(CaseInsensitiveMap<IEvaluator> evaluatorMap) {
        this.evaluatorMap = evaluatorMap;
    }

    @Override
    public Set<State> execute(RuntimeContext context) {
        ProcessDefinition processDefinition = context.getProcessDefinition();
        State currentState = context.getCurrentState();
        IEvaluator evaluator = evaluatorMap.get(currentState.getNextStateConditionScriptType());
        Set<State> nextStates = new HashSet<>();
        if (evaluator.evaluate(currentState.getNextStateCondition(), context)) {
            State nextState = processDefinition.getState(currentState.getNextState());
            nextStates.add(nextState);
        } else {
            nextStates.add(currentState);
        }
        return nextStates;
    }

    @Override
    public String getName() {
        return "ParallelEndGateway";
    }
}
