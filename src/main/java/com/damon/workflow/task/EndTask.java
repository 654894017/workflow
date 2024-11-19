package com.damon.workflow.task;


import com.damon.workflow.IProcessor;
import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.utils.CaseInsensitiveMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class EndTask implements ITask {

    private final Logger logger = LoggerFactory.getLogger(EndTask.class);
    private final CaseInsensitiveMap<IProcessor> processorsMap;

    public EndTask(CaseInsensitiveMap<IProcessor> processorsMap) {
        this.processorsMap = processorsMap;
    }


    @Override
    public Set<State> execute(RuntimeContext context) {
        ProcessDefinition processDefinition = context.getProcessDefinition();
        State currentState = context.getCurrentState();
        logger.info("processId: {}, {}: {} is finshed, variables: {}",
                processDefinition.getIdentifier(), getName(), currentState.getId(), currentState.getNextState(),
                context.getVariables());
        processState(currentState.getId(), context);
        return new HashSet<>();

    }

    private void processState(String currentStateId, RuntimeContext context) {
        IProcessor processor = processorsMap.get(currentStateId);
        if (processor != null) {
            processor.process(context);
        }
    }

    @Override
    public String getName() {
        return "End";
    }


}