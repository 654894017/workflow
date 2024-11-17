package com.damon.workflow.task;


import com.damon.workflow.IProcessor;
import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.utils.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class UserTask implements ITask {
    private final Logger logger = LoggerFactory.getLogger(UserTask.class);
    private final CaseInsensitiveMap<IProcessor> processorsMap;

    public UserTask(CaseInsensitiveMap<IProcessor> processorsMap) {
        this.processorsMap = processorsMap;
    }

    @Override
    public Object execute(RuntimeContext context) {
        ProcessDefinition processDefinition = context.getProcessDefinition();
        State currentState = context.getCurrentState();
        Object result = getStateProcessResult(currentState.getId(), context);
        logger.info("processId: {}, {}: {} is finshed, next state id: {}, variables: {}",
                processDefinition.getId(), getName(), currentState.getId(), currentState.getNextState(),
                context.getVariables());
        return result;
    }

    @Override
    public String getName() {
        return "UserTask";
    }

    private Object getStateProcessResult(String currentStateId, RuntimeContext context) {
        return Optional.ofNullable(processorsMap.get(currentStateId))
                .map(processor -> processor.process(context))
                .orElse(null);
    }
}