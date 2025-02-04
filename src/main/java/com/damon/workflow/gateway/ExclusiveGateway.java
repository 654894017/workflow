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
import com.damon.workflow.utils.StrUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class ExclusiveGateway implements IGateway {

    private final IEvaluator evaluator;

    @Override
    public List<State> execute(RuntimeContext context) {
        ProcessDefinition processDefinition = context.getProcessDefinition();
        State gatewayState = processDefinition.getState(context.getCurrentStateIdentifier().getCurrentStateId());
        List<State> nextStates = new ArrayList<>();
        for (Condition condition : gatewayState.getConditions()) {
            boolean result;
            if (StrUtils.isNotEmpty(condition.getNextStateConditionParser())) {
                IConditionParser conditionParser = ApplicationContextHelper.getBean(condition.getNextStateConditionParser());
                if (conditionParser == null) {
                    throw new ProcessException("未找到条件解析器: " + condition.getNextStateConditionParser());
                }
                result = conditionParser.test(context);
                log.info("processId: {}, {}: {}, result: {}, conditionType: {}, variables: {}",
                        processDefinition.getIdentifier(), getName(), gatewayState.getId(), result, condition.getNextStateConditionParser(),
                        context.getVariables());
            } else {
                String scriptType = StrUtils.isEmpty(condition.getScriptType()) ? ProcessConstant.DEFAULT_EVALUATOR : condition.getScriptType();
                result = evaluator.evaluate(condition.getCondition(), scriptType, context);
                log.info("processId: {}, {}: {}, result: {}, condition: {}, variables: {}",
                        processDefinition.getIdentifier(), getName(), gatewayState.getId(), result, condition.getCondition(),
                        context.getVariables());
            }
            if (result) {
                State nextState = processDefinition.getState(condition.getNextStateId());
                nextStates.add(nextState);
                return nextStates;
            }
        }
        throw new ProcessException("未满足任何条件的网关节点: " + gatewayState.getId());
    }

    @Override
    public String getName() {
        return "ExclusiveGateway";
    }
}
