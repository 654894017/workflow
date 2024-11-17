package com.damon.workflow.task;


import com.damon.workflow.IProcessor;
import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.utils.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserTask implements ITask {
    private final Logger logger = LoggerFactory.getLogger(UserTask.class);
    private final CaseInsensitiveMap<IProcessor> processorsMap;

    public UserTask(CaseInsensitiveMap<IProcessor> processorsMap) {
        this.processorsMap = processorsMap;
    }

    @Override
    public void execute(RuntimeContext context) {
        ProcessDefinition processDefinition = context.getProcessDefinition();
        State currentState = context.getCurrentState();
        processState(currentState.getId(), context);
        logger.info("processId: {}, {}: {} is finshed, nextStateId: {}, variables: {}",
                processDefinition.getId(), getName(), currentState.getId(), currentState.getNextState(),
                context.getVariables());
    }

    @Override
    public String getName() {
        return "UserTask";
    }

    private void processState(String currentStateId, RuntimeContext context) {
        IProcessor processor = processorsMap.get(currentStateId);
        if (processor != null) {
            processor.process(context);
        }
    }
}