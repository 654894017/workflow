package com.damon.workflow.task;


import com.damon.workflow.ProcessConstant;
import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.exception.ProcessTaskException;
import com.damon.workflow.process.IProcessor;
import com.damon.workflow.spring.ApplicationContextHelper;
import com.damon.workflow.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class EndTask implements ITask {
    @Override
    public List<State> execute(RuntimeContext context) {
        ProcessDefinition processDefinition = context.getProcessDefinition();
        State currentState = processDefinition.getState(context.getCurrentStateIdentifier().getCurrentStateId());
        log.info("processId: {}, {}: {} is finshed, variables: {}",
                processDefinition.getIdentifier(), getName(), currentState.getId(), currentState.getNextStateId(),
                context.getVariables());
        if (CollUtils.isNotEmpty(currentState.getProcessors())) {
            currentState.getProcessors().forEach(processorClassName ->
                    // 执行当前状态处理逻辑
                    processState(processorClassName, currentState, context)
            );
        }
        return new ArrayList<>(0);
    }

    private void processState(String processorClassName, State currentState, RuntimeContext context) {
        IProcessor processor = ApplicationContextHelper.getBean(processorClassName);
        if (processor != null && processor.isMatch(context)) {
            try {
                processor.process(context);
                Map<String, Object> variables = context.getVariables();
                context.setStateProcessResult(variables.get(ProcessConstant.STATE_PROCESS_RESULT));
            } catch (Throwable e) {
                log.error("流程ID: {}, 当前状态: {} 处理失败", context.getProcessDefinition().getIdentifier(),
                        context.getCurrentStateIdentifier().getFullPaths(), e);
                throw new ProcessTaskException(context.getCurrentStateIdentifier(), e);
            }
        }
    }

    @Override
    public String getName() {
        return "End";
    }
}