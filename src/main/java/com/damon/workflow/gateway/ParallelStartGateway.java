package com.damon.workflow.gateway;

import com.damon.workflow.ProcessConstant;
import com.damon.workflow.RuntimeContext;
import com.damon.workflow.condition_parser.IConditionParser;
import com.damon.workflow.config.Condition;
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
public class ParallelStartGateway implements IGateway {

    private final CaseInsensitiveMap<IEvaluator> evaluatorMap;

    @Override
    public Set<State> execute(RuntimeContext context) {
        Set<State> nextStates = new HashSet<>();
        State state = context.getCurrentState();
        ProcessDefinition processDefinition = context.getProcessDefinition();
        for (Condition condition : state.getConditions()) {
            boolean result;
            if (StrUtils.isNotEmpty(condition.getNextStateConditionParser())) {
                IConditionParser conditionParser = ApplicationContextHelper.getBean(condition.getNextStateConditionParser());
                if (conditionParser == null) {
                    throw new ProcessException("未找到条件解析器: " + condition.getNextStateConditionParser());
                }
                result = conditionParser.test(context);
            } else {
                String scriptType = StrUtils.isEmpty(condition.getScriptType()) ? ProcessConstant.DEFAULT_EVALUATOR : condition.getScriptType();
                IEvaluator evaluator = evaluatorMap.get(scriptType);
                if (evaluator == null) {
                    throw new ProcessException("未找到脚本执行器: " + condition.getScriptType());
                }
                result = evaluator.evaluate(condition.getCondition(), context);
            }

            if (StrUtils.isEmpty(condition.getCondition()) || result) {
                State nextState = processDefinition.getState(condition.getNextStateId());
                nextStates.add(nextState);
            }
        }
        nextStates.forEach(nextState -> {
            log.info("processId: {}, {}: {}, nextState:{}, variables: {}",
                    processDefinition.getIdentifier(), getName(), state.getId(), nextState.getId(), context.getVariables()
            );
        });
        return nextStates;
    }

    @Override
    public String getName() {
        return "ParallelStartGateway";
    }
}
