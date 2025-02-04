package com.damon.workflow.gateway;

import com.damon.workflow.ProcessConstant;
import com.damon.workflow.RuntimeContext;
import com.damon.workflow.condition_parser.IConditionParser;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.evaluator.IEvaluator;
import com.damon.workflow.exception.ProcessException;
import com.damon.workflow.spring.ApplicationContextHelper;
import com.damon.workflow.utils.StrUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ParallelEndGateway implements IGateway {

    private final IEvaluator evaluator;

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
        return "ParallelEndGateway";
    }
}
