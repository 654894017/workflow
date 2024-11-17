package com.damon.workflow.gateway;

import com.damon.workflow.IConditionParser;
import com.damon.workflow.RuntimeContext;
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

public class ParallelEndGateway implements IGateway {

    private final Logger logger = LoggerFactory.getLogger(ParallelEndGateway.class);

    private final CaseInsensitiveMap<IEvaluator> evaluatorMap;

    private final CaseInsensitiveMap<IConditionParser> conditionMap;

    public ParallelEndGateway(CaseInsensitiveMap<IEvaluator> evaluatorMap, CaseInsensitiveMap<IConditionParser> conditionMap) {
        this.evaluatorMap = evaluatorMap;
        this.conditionMap = conditionMap;
    }

    @Override
    public Set<State> execute(RuntimeContext context) {
        ProcessDefinition processDefinition = context.getProcessDefinition();
        String processId = processDefinition.getId();
        State currentState = context.getCurrentState();
        Set<State> nextStates = new HashSet<>();
        boolean result;
        if (StrUtils.isNotEmpty(currentState.getNextStateConditionParser())) {
            IConditionParser conditionParser = conditionMap.get(currentState.getNextStateConditionParser());
            if (conditionParser == null) {
                throw new ProcessException("未找到条件解析器: " + currentState.getNextStateConditionParser());
            }
            result = conditionParser.test(context);
        } else {
            IEvaluator evaluator = evaluatorMap.get(currentState.getNextStateConditionScriptType());
            if (evaluator == null) {
                throw new ProcessException("未找到脚本执行器: " + currentState.getNextStateConditionScriptType());
            }
            result = evaluator.evaluate(currentState.getNextStateCondition(), context);
        }
        if (result) {
            State nextState = processDefinition.getState(currentState.getNextState());
            nextStates.add(nextState);
        } else {
            nextStates.add(currentState);
        }
        nextStates.forEach(nextState -> {
            logger.info("processId: {}, {}: {}, nextState:{}, result: {}, variables: {}",
                    processDefinition.getId(), getName(), currentState.getId(), nextState.getId(),
                    result,
                    context.getVariables()
            );
        });
        return nextStates;
    }

    @Override
    public String getName() {
        return "ParallelEndGateway";
    }
}
