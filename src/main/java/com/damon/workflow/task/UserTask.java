package com.damon.workflow.task;


import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserTask implements ITask {
    private final Logger logger = LoggerFactory.getLogger(UserTask.class);

    @Override
    public State execute(RuntimeContext context) {
        ProcessDefinition processDefinition = context.getProcessDefinition();
        State currentState = context.getCurrentState();
        State nextState = processDefinition.getState(currentState.getNextState());
        logger.info("processId: {}, {}: {} is finshed, next state id: {}, variables: {}",
                processDefinition.getId(), getName(), currentState.getId(), currentState.getNextState(),
                context.getVariables());
        return nextState;
    }

    @Override
    public String getName() {
        return "UserTask";
    }
}