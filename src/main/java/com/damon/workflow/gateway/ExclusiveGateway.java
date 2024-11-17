package com.damon.workflow.gateway;

import com.damon.workflow.IConditionParser;
import com.damon.workflow.ProcessConstant;
import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.Condition;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.evaluator.IEvaluator;
import com.damon.workflow.exception.ProcessException;
import com.damon.workflow.utils.CaseInsensitiveMap;
import com.damon.workflow.utils.StrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;


public class ExclusiveGateway implements IGateway {
    private final Logger logger = LoggerFactory.getLogger(ExclusiveGateway.class);

    private final CaseInsensitiveMap<IEvaluator> evaluatorMap;
    private final CaseInsensitiveMap<IConditionParser> conditionMap;

    public ExclusiveGateway(CaseInsensitiveMap<IEvaluator> evaluatorMap, CaseInsensitiveMap<IConditionParser> conditionMap) {
        this.evaluatorMap = evaluatorMap;
        this.conditionMap = conditionMap;
    }


    @Override
    public Set<State> execute(RuntimeContext context) {
        ProcessDefinition processDefinition = context.getProcessDefinition();
        State gatewayState = context.getCurrentState();
        Set<State> nextStates = new HashSet<>();
        for (Condition condition : gatewayState.getConditions()) {
            boolean result;
            if (StrUtils.isNotEmpty(condition.getNextStateConditionParser())) {
                IConditionParser conditionParser = conditionMap.get(condition.getNextStateConditionParser());
                if (conditionParser == null) {
                    throw new ProcessException("未找到条件解析器: " + condition.getNextStateConditionParser());
                }
                result = conditionParser.test(context);
                logger.info("processId: {}, {}: {}, result: {}, conditionType: {}, variables: {}",
                        processDefinition.getId(), getName(), gatewayState.getId(), result, condition.getNextStateConditionParser(),
                        context.getVariables());
            } else {
                String scriptType = StrUtils.isEmpty(condition.getScriptType()) ? ProcessConstant.DEFAULT_EVALUATOR : condition.getScriptType();
                IEvaluator evaluator = evaluatorMap.get(scriptType);
                if (evaluator == null) {
                    throw new ProcessException("未找到脚本执行器: " + condition.getScriptType());
                }
                result = evaluator.evaluate(condition.getCondition(), context);
                logger.info("processId: {}, {}: {}, result: {}, condition: {}, variables: {}",
                        processDefinition.getId(), getName(), gatewayState.getId(), result, condition.getCondition(),
                        context.getVariables());
            }
            if (result) {
                State nextState = processDefinition.getState(condition.getNextState());
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
