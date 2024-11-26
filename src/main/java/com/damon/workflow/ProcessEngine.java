package com.damon.workflow;

import com.damon.workflow.config.State;
import com.damon.workflow.exception.ProcessException;
import com.damon.workflow.utils.ClasspathFileUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessEngine {

    private Map<String, ProcessInstance> instanceMap = new ConcurrentHashMap<>();

    public ProcessEngine() {
    }

    public String registerProcessInstance(String classpathYamlFile) {
        String content = ClasspathFileUtils.readFileAsString(classpathYamlFile);
        return registerProcessInstance(ProcessInstance.load(content));
    }

    public String registerProcessInstance(ProcessInstance instance) {
        String identifier = instance.getProcessDefinition().getIdentifier();
        if (instanceMap.containsKey(instance.getProcessDefinition().getIdentifier())) {
            throw new ProcessException("流程定义ID重复定义，请检查配置文件，processId: " + identifier);
        }
        instanceMap.put(identifier, instance);
        return identifier;
    }

    public State getState(String identifier, String stateId) {
        ProcessInstance instance = instanceMap.get(identifier);
        return instance.getProcessDefinition().getState(stateId);
    }

    public ProcessResult process(String identifier, Map<String, Object> variables) {
        return process(identifier, variables, null);
    }

    public ProcessResult process(String identifier, Map<String, Object> variables, String businessId) {
        ProcessInstance instance = instanceMap.get(identifier);
        return instance.process(variables);
    }

    public ProcessResult process(String identifier, String currentStateId, Map<String, Object> variables) {
        return process(identifier, currentStateId, variables, null);
    }

    public ProcessResult process(String identifier, String currentStateId, Map<String, Object> variables, String businessId) {
        ProcessInstance instance = instanceMap.get(identifier);
        return instance.process(currentStateId, variables);
    }


}
