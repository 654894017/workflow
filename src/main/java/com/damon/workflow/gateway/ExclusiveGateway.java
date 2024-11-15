package com.damon.workflow.gateway;

import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.Condition;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.evaluator.IEvaluator;
import com.damon.workflow.exception.ProcessException;
import com.damon.workflow.utils.CaseInsensitiveMap;
import com.damon.workflow.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;


public class ExclusiveGateway implements IGateway {
    private final Logger logger = LoggerFactory.getLogger(ExclusiveGateway.class);

    private final CaseInsensitiveMap<IEvaluator> evaluatorMap;

    public ExclusiveGateway(CaseInsensitiveMap<IEvaluator> evaluatorMap) {
        this.evaluatorMap = evaluatorMap;
    }

    @Override
    public Set<State> execute(RuntimeContext context) {
        ProcessDefinition processDefinition = context.getProcessDefinition();
        State gatewayState = context.getCurrentState();
        Set<State> nextStates = new HashSet<>();
        for (Condition condition : gatewayState.getConditions()) {
            IEvaluator evaluator = evaluatorMap.get(condition.getScriptType());
            boolean result = evaluator.evaluate(condition.getCondition(), context);
            logger.info("processId: {}, {}: {}, result: {}, condition: {}, variables: {}",
                    processDefinition.getId(), getName(), gatewayState.getId(), result, condition.getCondition(),
                    context.getVariables());
            if (result) {
                State nextState = processDefinition.getState(condition.getNextState());
                nextStates.add(nextState);
            }
        }
        if (CollectionUtils.isEmpty(nextStates)) {
            throw new ProcessException("未满足任何条件的网关节点: " + gatewayState.getId());
        }
        return nextStates;
    }

    @Override
    public String getName() {
        return "ExclusiveGateway";
    }
}
