package com.damon.workflow.task;


import com.damon.workflow.condition_parser.IProcessor;
import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.utils.StrUtils;
import com.damon.workflow.utils.spring.ApplicationContextHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class EndTask implements ITask {

    private final Logger logger = LoggerFactory.getLogger(EndTask.class);

    @Override
    public Set<State> execute(RuntimeContext context) {
        ProcessDefinition processDefinition = context.getProcessDefinition();
        State currentState = context.getCurrentState();
        logger.info("processId: {}, {}: {} is finshed, variables: {}",
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