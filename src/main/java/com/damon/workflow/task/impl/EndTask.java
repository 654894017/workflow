package com.damon.workflow.task.impl;


import com.damon.workflow.ProcessConstant;
import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.exception.ProcessTaskException;
import com.damon.workflow.handler.IProcessStateHandler;
import com.damon.workflow.handler.ProcessHandlerFactory;
import com.damon.workflow.task.ITask;
import com.damon.workflow.utils.CollUtils;
import com.damon.workflow.utils.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class EndTask implements ITask {

    private final Logger log = LoggerFactory.getLogger(EndTask.class);

    @Override
    public List<State> execute(RuntimeContext context) {
        ProcessDefinition processDefinition = context.getDefinition();
        State state = processDefinition.getState(context.getCurrentStateIdentifier().getCurrentStateId());
        log.info("ProcessId: {}, {}: {} is finshed, variables: {}",
                processDefinition.getIdentifier(), getName(), state.getId(), state.getNextStateId(),
                context.getVariables());
        if (CollUtils.isNotEmpty(state.getHandlers())) {
            state.getHandlers().forEach(processorClassName ->
                    // 执行当前状态处理逻辑
                    processState(processorClassName, state, context)
            );
        }
        return Lists.createEmptyList();
    }

    private void processState(String processorClassName, State currentState, RuntimeContext context) {
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

    @Override
    public String getName() {
        return ProcessConstant.END;
    }
}