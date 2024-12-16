package com.damon.workflow.task;


import com.damon.workflow.RuntimeContext;
import com.damon.workflow.condition_parser.IProcessor;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.spring.ApplicationContextHelper;
import com.damon.workflow.utils.CollUtils;
import com.damon.workflow.utils.StrUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class EndTask implements ITask {
    @Override
    public List<State> execute(RuntimeContext context) {
        ProcessDefinition processDefinition = context.getProcessDefinition();
        State currentState = context.getCurrentState();
        log.info("processId: {}, {}: {} is finshed, variables: {}",
                processDefinition.getIdentifier(), getName(), currentState.getId(), currentState.getNextStateId(),
                context.getVariables());
        if (CollUtils.isNotEmpty(currentState.getProcessors())) {
            currentState.getProcessors().forEach(processorClassName ->
                    // 执行当前状态处理逻辑
                    processState(processorClassName, context)
            );
        }
        return new ArrayList<>(0);
    }

    private void processState(String processorClassName, RuntimeContext context) {
        IProcessor processor = ApplicationContextHelper.getBean(processorClassName);
        if (processor != null && processor.isMatch(context.getCurrentState(), context.getVariables())) {
            processor.process(context);
        }
    }

    @Override
    public String getName() {
        return "End";
    }
}