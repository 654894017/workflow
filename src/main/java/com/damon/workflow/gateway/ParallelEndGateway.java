package com.damon.workflow.gateway;

import com.damon.workflow.ProcessConstant;
import com.damon.workflow.RuntimeContext;
import com.damon.workflow.conditionparser.IConditionParser;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.evaluator.IEvaluator;
import com.damon.workflow.exception.ProcessException;
import com.damon.workflow.spring.ApplicationContextHelper;
import com.damon.workflow.utils.StrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ParallelEndGateway implements IGateway {
    private final Logger log = LoggerFactory.getLogger(ParallelEndGateway.class);

    private final IEvaluator evaluator;

    public ParallelEndGateway(IEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public List<State> execute(RuntimeContext context) {
        ProcessDefinition processDefinition = context.getProcessDefinition();
        State currentState = processDefinition.getState(context.getCurrentStateIdentifier().getCurrentStateId());
        List<State> nextStates = new ArrayList<>();
        boolean result;
        if (StrUtils.isNotEmpty(currentState.getNextStateConditionParser())) {
            IConditionParser conditionParser = ApplicationContextHelper.getBean(currentState.getNextStateConditionParser());
            if (conditionParser == null) {
                throw new ProcessException("未找到条件解析器: " + currentState.getNextStateConditionParser());
            }
            result = conditionParser.test(context);
        } else {
            String nextStateConditionScriptType = currentState.getNextStateConditionScriptType();
            String scriptType = StrUtils.isEmpty(nextStateConditionScriptType) ? ProcessConstant.DEFAULT_EVALUATOR : nextStateConditionScriptType;
            result = evaluator.evaluate(currentState.getNextStateCondition(), scriptType, context);
        }
        if (result) {
            State nextState = processDefinition.getState(currentState.getNextStateId());
            nextStates.add(nextState);
        } else {
            nextStates.add(currentState);
        }
        nextStates.forEach(nextState -> {
            log.info("processId: {}, {}: {}, nextState:{}, result: {}, variables: {}",
                    processDefinition.getIdentifier(), getName(), currentState.getId(), nextState.getId(), result, context.getVariables()
            );
        });
        return nextStates;
    }

    @Override
    public String getName() {
        return ProcessConstant.PARALLEL_END_GATEWAY;
    }
}
