package com.damon.workflow.gateway;

import com.damon.workflow.ProcessConstant;
import com.damon.workflow.RuntimeContext;
import com.damon.workflow.conditionparser.ConditionParserFactory;
import com.damon.workflow.conditionparser.IConditionParser;
import com.damon.workflow.config.Condition;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.evaluator.IEvaluator;
import com.damon.workflow.exception.ProcessException;
import com.damon.workflow.utils.StrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ParallelStartGateway implements IGateway {

    private final Logger log = LoggerFactory.getLogger(ParallelStartGateway.class);

    private final IEvaluator evaluator;

    public ParallelStartGateway(IEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public List<State> execute(RuntimeContext context) {
        List<State> nextStates = new ArrayList<>();
        State state = context.getDefinition().getState(context.getCurrentStateIdentifier().getCurrentStateId());
        ProcessDefinition processDefinition = context.getDefinition();
        for (Condition condition : state.getConditions()) {
            boolean result;
            if (StrUtils.isNotEmpty(condition.getNextStateConditionParser())) {
                IConditionParser conditionParser = ConditionParserFactory.getConditionParser(condition.getNextStateConditionParser());
                if (conditionParser == null) {
                    throw new ProcessException("未找到条件解析器: " + condition.getNextStateConditionParser());
                }
                result = conditionParser.test(context);
            } else {
                String scriptType = StrUtils.isEmpty(condition.getScriptType()) ? ProcessConstant.DEFAULT_EVALUATOR : condition.getScriptType();
                result = evaluator.evaluate(condition.getCondition(), scriptType, context);
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
        return ProcessConstant.PARALLEL_START_GATEWAY;
    }
}
