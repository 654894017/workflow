package com.damon.workflow.task.impl;


import com.damon.workflow.ProcessConstant;
import com.damon.workflow.RuntimeContext;
import com.damon.workflow.conditionparser.ConditionParserFactory;
import com.damon.workflow.conditionparser.IConditionParser;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.evaluator.IEvaluator;
import com.damon.workflow.exception.ProcessException;
import com.damon.workflow.exception.ProcessTaskException;
import com.damon.workflow.handler.IProcessStateHandler;
import com.damon.workflow.handler.ProcessHandlerFactory;
import com.damon.workflow.task.ITask;
import com.damon.workflow.utils.CollUtils;
import com.damon.workflow.utils.StrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeneralTask implements ITask {
    private final Logger log = LoggerFactory.getLogger(GeneralTask.class);

    private final IEvaluator evaluator;

    public GeneralTask(IEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public List<State> execute(RuntimeContext context) {
        ProcessDefinition processDefinition = context.getDefinition();
        State currentState = processDefinition.getState(context.getCurrentStateIdentifier().getCurrentStateId());
        if (CollUtils.isNotEmpty(currentState.getHandlers())) {
            currentState.getHandlers().forEach(processorClassName ->
                    // 执行当前状态处理逻辑
                    processCurrentState(processorClassName, context)
            );
        }

        // 条件解析，判断下一状态
        List<State> nextStates = determineNextStates(context, currentState, processDefinition);

        // 记录日志
        logTransitions(processDefinition, currentState, nextStates, context);

        return nextStates;
    }

    @Override
    public String getName() {
        return ProcessConstant.GENERAL_TASK;
    }

    /**
     * 处理当前状态逻辑
     */
    private void processCurrentState(String processorClassName, RuntimeContext context) {
        IProcessStateHandler handler = ProcessHandlerFactory.getProcessHandler(processorClassName);
        if (handler != null && handler.isMatch(context)) {
            try {
                handler.handle(context);
                Map<String, Object> variables = context.getVariables();
                context.setResult(variables.get(ProcessConstant.STATE_PROCESS_RESULT));
            } catch (Exception e) {
                log.error("流程ID: {}, 当前状态: {} 处理失败", context.getDefinition().getIdentifier(),
                        context.getCurrentStateIdentifier().getFullPaths(), e);
                throw new ProcessTaskException(context.getCurrentStateIdentifier(), e);
            }
        }
    }

    /**
     * 确定下一状态集合
     */
    private List<State> determineNextStates(RuntimeContext context, State currentState, ProcessDefinition definition) {
        List<State> nextStates = new ArrayList<>();

        boolean result = evaluateCondition(context, currentState);

        if (result) {
            State nextState = definition.getState(currentState.getNextStateId());
            if (nextState != null) {
                nextStates.add(nextState);
            } else {
                log.warn("流程ID: {}, 当前状态: {} 的下一状态未定义或无效", definition.getIdentifier(), currentState.getId());
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
            IConditionParser conditionParser = ConditionParserFactory.getConditionParser(currentState.getNextStateConditionParser());
            if (conditionParser == null) {
                throw new ProcessException("No script executor was found: " + currentState.getNextStateConditionParser());
            }
            return conditionParser.test(context);
        }

        // 使用脚本执行器
        if (StrUtils.isNotEmpty(currentState.getNextStateCondition())) {
            String scriptType = StrUtils.isEmpty(currentState.getNextStateConditionScriptType())
                    ? ProcessConstant.DEFAULT_EVALUATOR
                    : currentState.getNextStateConditionScriptType();
            if (evaluator == null) {
                throw new ProcessException("No script executor was found: " + scriptType);
            }
            return evaluator.evaluate(currentState.getNextStateCondition(), scriptType, context);
        }

        // 默认条件
        return true;
    }

    /**
     * 记录状态流转日志
     */
    private void logTransitions(ProcessDefinition processDefinition, State currentState,
                                List<State> nextStates, RuntimeContext context) {
        nextStates.forEach(nextState -> log.info("流程ID: {}, 节点类型: {}, 当前状态: {}, 下一状态: {}, 变量: {}",
                processDefinition.getIdentifier(), getName(), currentState.getId(), nextState.getId(), context.getVariables()));
    }
}