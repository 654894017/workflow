package com.damon.workflow.task;


import com.damon.workflow.ProcessConstant;
import com.damon.workflow.RuntimeContext;
import com.damon.workflow.condition_parser.IConditionParser;
import com.damon.workflow.condition_parser.IProcessor;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.evaluator.IEvaluator;
import com.damon.workflow.exception.ProcessException;
import com.damon.workflow.utils.CaseInsensitiveMap;
import com.damon.workflow.utils.StrUtils;
import com.damon.workflow.utils.spring.ApplicationContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class UserTask implements ITask {
    private static final Logger logger = LoggerFactory.getLogger(UserTask.class);

    private final CaseInsensitiveMap<IEvaluator> evaluatorMap;

    public UserTask(CaseInsensitiveMap<IEvaluator> evaluatorMap) {
        this.evaluatorMap = evaluatorMap;
    }

    @Override
    public Set<State> execute(RuntimeContext context) {
        ProcessDefinition processDefinition = context.getProcessDefinition();
        State currentState = context.getCurrentState();
        if (StrUtils.isNotEmpty(currentState.getProcessor())) {
            // 执行当前状态处理逻辑
            processCurrentState(currentState.getProcessor(), context);
        }

        // 条件解析，判断下一状态
        Set<State> nextStates = determineNextStates(context, currentState, processDefinition);

        // 记录日志
        logTransitions(processDefinition, currentState, nextStates, context);

        return nextStates;
    }

    @Override
    public String getName() {
        return "UserTask";
    }

    /**
     * 处理当前状态逻辑
     */
    private void processCurrentState(String processorClassName, RuntimeContext context) {
        IProcessor processor = ApplicationContextHelper.getBean(processorClassName);
        if (processor != null) {
            processor.process(context);
        }
    }

    /**
     * 确定下一状态集合
     */
    private Set<State> determineNextStates(RuntimeContext context, State currentState, ProcessDefinition definition) {
        Set<State> nextStates = new HashSet<>();

        boolean result = evaluateCondition(context, currentState);

        if (result) {
            State nextState = definition.getState(currentState.getNextStateId());
            if (nextState != null) {
                nextStates.add(nextState);
            } else {
                logger.warn("流程ID: {}, 当前状态: {} 的下一状态未定义或无效", definition.getIdentifier(), currentState.getId());
            }
        } else {
            // 条件不满足，停留在当前状态
            nextStates.add(currentState);
        }

        return nextStates;
    }

    /**
     * 根据条件解析器或脚本执行器评估条件
     */
    private boolean evaluateCondition(RuntimeContext context, State currentState) {
        // 优先使用条件解析器
        if (StrUtils.isNotEmpty(currentState.getNextStateConditionParser())) {
            IConditionParser conditionParser = ApplicationContextHelper.getBean(currentState.getNextStateConditionParser());
            if (conditionParser == null) {
                throw new ProcessException("未找到条件解析器: " + currentState.getNextStateConditionParser());
            }
            return conditionParser.test(context);
        }

        // 使用脚本执行器
        if (StrUtils.isNotEmpty(currentState.getNextStateCondition())) {
            String scriptType = StrUtils.isEmpty(currentState.getNextStateConditionScriptType())
                    ? ProcessConstant.DEFAULT_EVALUATOR
                    : currentState.getNextStateConditionScriptType();
            IEvaluator evaluator = evaluatorMap.get(scriptType);
            if (evaluator == null) {
                throw new ProcessException("未找到脚本执行器: " + scriptType);
            }
            return evaluator.evaluate(currentState.getNextStateCondition(), context);
        }

        // 默认条件
        return true;
    }

    /**
     * 记录状态流转日志
     */
    private void logTransitions(ProcessDefinition processDefinition, State currentState,
                                Set<State> nextStates, RuntimeContext context) {
        nextStates.forEach(nextState -> logger.info("流程ID: {}, 任务: {}, 当前状态: {}, 下一状态: {}, 变量: {}",
                processDefinition.getIdentifier(), getName(), currentState.getId(), nextState.getId(), context.getVariables()));
    }
}