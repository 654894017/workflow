package com.damon.workflow.gateway;

import com.damon.workflow.ProcessConstant;
import com.damon.workflow.RuntimeContext;
import com.damon.workflow.condition_parser.IConditionParser;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.evaluator.IEvaluator;
import com.damon.workflow.exception.ProcessException;
import com.damon.workflow.spring.ApplicationContextHelper;
import com.damon.workflow.utils.CaseInsensitiveMap;
import com.damon.workflow.utils.StrUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class ParallelEndGateway implements IGateway {

    private final CaseInsensitiveMap<IEvaluator> evaluatorMap;

    @Override
    public Set<State> execute(RuntimeContext context) {
        ProcessDefinition processDefinition = context.getProcessDefinition();
        State currentState = context.getCurrentState();
        Set<State> nextStates = new HashSet<>();
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
            IEvaluator evaluator = evaluatorMap.get(scriptType);
            if (evaluator == null) {
                throw new ProcessException("未找到脚本执行器: " + currentState.getNextStateConditionScriptType());
            }
            result = evaluator.evaluate(currentState.getNextStateCondition(), context);
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
