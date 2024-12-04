package com.damon.workflow.task;


import com.damon.workflow.RuntimeContext;
import com.damon.workflow.condition_parser.IProcessor;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.spring.ApplicationContextHelper;
import com.damon.workflow.utils.StrUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class EndTask implements ITask {
    @Override
    public Set<State> execute(RuntimeContext context) {
        ProcessDefinition processDefinition = context.getProcessDefinition();
        State currentState = context.getCurrentState();
        log.info("processId: {}, {}: {} is finshed, variables: {}",
                processDefinition.getIdentifier(), getName(), currentState.getId(), currentState.getNextStateId(),
                context.getVariables());
        if (StrUtils.isNotEmpty(currentState.getProcessor())) {
            processState(currentState.getProcessor(), context);
        }
        return new HashSet<>();
    }

    private void processState(String processorClassName, RuntimeContext context) {
        IProcessor processor = ApplicationContextHelper.getBean(processorClassName);
        if (processor != null) {
            processor.process(context);
        }
    }

    @Override
    public String getName() {
        return "End";
    }
}