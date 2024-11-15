package com.damon.workflow.task;


import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndTask implements ITask {

    private final Logger logger = LoggerFactory.getLogger(EndTask.class);

    @Override
    public State execute(RuntimeContext context) {
        ProcessDefinition processDefinition = context.getProcessDefinition();
        logger.info("processId: {}, end process, variables : {}", processDefinition.getId(), context.getVariables());
        return context.getCurrentState();
    }

    @Override
    public String getName() {
        return "End";
    }


}