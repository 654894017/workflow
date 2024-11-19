package com.damon.workflow;

import com.damon.workflow.exception.ProcessException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessEngine {

    private Map<String, ProcessInstance> instanceMap = new ConcurrentHashMap<>();

    public ProcessEngine() {
    }

    public void registerProcessInstance(ProcessInstance instance) {
        String identifier = instance.getProcessDefinition().getIdentifier();
        if (instanceMap.containsKey(instance.getProcessDefinition().getIdentifier())) {
            throw new ProcessException("流程定义ID重复定义，请检查配置文件，processId: " + identifier);
        }
        instanceMap.put(identifier, instance);
    }

    public ProcessResult process(String identifier, Map<String, Object> variables) {
        ProcessInstance instance = instanceMap.get(identifier);
        return instance.process(variables);
    }

    public ProcessResult process(String identifier, String currentStateId, Map<String, Object> variables) {
        ProcessInstance instance = instanceMap.get(identifier);
        return instance.process(currentStateId, variables);
    }


}
