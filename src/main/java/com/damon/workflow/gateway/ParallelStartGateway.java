package com.damon.workflow.gateway;

import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.Condition;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.evaluator.IEvaluator;
import com.damon.workflow.utils.CaseInsensitiveMap;
import com.damon.workflow.utils.StrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class ParallelStartGateway implements IGateway {
    private final Logger logger = LoggerFactory.getLogger(ParallelStartGateway.class);

    private final CaseInsensitiveMap<IEvaluator> evaluatorMap;

    public ParallelStartGateway(CaseInsensitiveMap<IEvaluator> evaluatorMap) {
        this.evaluatorMap = evaluatorMap;
    }

    @Override
    public Set<State> execute(RuntimeContext context) {
        Set<State> nextStates = new HashSet<>();
        State state = context.getCurrentState();
        ProcessDefinition processDefinition = context.getProcessDefinition();
        for (Condition condition : state.getConditions()) {
            IEvaluator evaluator = evaluatorMap.get(condition.getScriptType());
            if (StrUtils.isEmpty(condition.getCondition()) || evaluator.evaluate(condition.getCondition(), context)) {
                State nextState = processDefinition.getState(condition.getNextState());
                nextStates.add(nextState);
            }
        }
        return nextStates;
    }

    @Override
    public String getName() {
        return "ParallelEndGateway";
    }
}
