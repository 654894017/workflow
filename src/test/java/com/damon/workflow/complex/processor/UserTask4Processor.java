package com.damon.workflow.complex.processor;

import com.damon.workflow.IProcessor;
import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.State;
import com.damon.workflow.utils.Sets;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UserTask4Processor implements IProcessor<Map<String, Object>> {
    @Override
    public Map<String, Object> process(RuntimeContext context) {
        HashMap<String, Object> map = new HashMap<>();
        State state = context.getCurrentState();
        map.put(state.getId(), state.getType());
        return map;
    }

    @Override
    public Set<String> stateIds() {
        return Sets.newHashSet("UserTask4");
    }
}
