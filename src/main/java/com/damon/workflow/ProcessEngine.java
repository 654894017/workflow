package com.damon.workflow;

import com.damon.workflow.config.NextState;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.config.StateIdentifier;
import com.damon.workflow.exception.ProcessException;
import com.damon.workflow.utils.classpath.ClasspathFileUtils;
import com.damon.workflow.utils.classpath.ClasspathFlowFileLoader;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ProcessEngine {

    private Map<String, ProcessInstance> instanceMap = new ConcurrentHashMap<>();

    public ProcessEngine() {
        ClasspathFlowFileLoader loader = new ClasspathFlowFileLoader();
        List<String> flows = loader.loadFilesFromFlowFolder();
        flows.forEach(content -> {
            String processIdentifier = registerProcessInstance(ProcessInstance.load(content));
            log.info("register process: [{}] succeeded", processIdentifier);
        });
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

    /**
     * 开启流程
     *
     * @param processIdentifier
     * @param variables
     * @return
     */
    public ComplexProcessResult process(String processIdentifier, Map<String, Object> variables) {
        return process(processIdentifier, variables, null);
    }

    /**
     * 开启流程
     *
     * @param processIdentifier
     * @param variables
     * @param businessId
     * @return
     */
    public ComplexProcessResult process(String processIdentifier, Map<String, Object> variables, String businessId) {
        ProcessInstance instance = getProcessInstance(processIdentifier);
        ProcessDefinition processDefinition = instance.getProcessDefinition();
        return process(StateIdentifier.buildByStateIdentifiers(processIdentifier, processDefinition.getStartStateId()), variables, businessId);
    }

    /**
     * @param currentStateIdentifier
     * @param variables
     * @return
     */
    public ComplexProcessResult process(StateIdentifier currentStateIdentifier, Map<String, Object> variables) {
        return process(currentStateIdentifier, variables, null);
    }

    /**
     * @param currentStateIdentifier
     * @param variables
     * @param businessId
     * @return
     */
    public ComplexProcessResult process(StateIdentifier currentStateIdentifier, Map<String, Object> variables, String businessId) {
        ComplexProcessResult result = doProcess(currentStateIdentifier, variables, businessId);
        result.setCurrentStateIndentifier(currentStateIdentifier);
        return result;
    }

    private ComplexProcessResult doProcess(StateIdentifier currentStateIdentifier, Map<String, Object> variables, String businessId) {
        ProcessResult result = process(currentStateIdentifier.getCurrentStateProcessIdentifier(), currentStateIdentifier.getCurrentStateId(), variables, businessId);
        if (result.isCompleted() && currentStateIdentifier.isSubProcess()) {
            State subProcessState = getState(currentStateIdentifier.getParentProcessIdentifier(), currentStateIdentifier.getSubProcessStateId());
            return doProcess(
                    StateIdentifier.build(currentStateIdentifier.getParentProcessFullPaths(), subProcessState.getNextStateId()),
                    variables, businessId
            );
        }
        State currentState = getState(currentStateIdentifier.getCurrentStateProcessIdentifier(), currentStateIdentifier.getCurrentStateId());
        List<NextState> nextStates = new ArrayList<>();
        result.getNextStates().forEach(state -> {
            StateIdentifier nextStateIdentifier = StateIdentifier.build(
                    currentStateIdentifier.getFullPathsExcludingLast(), state.getId()
            );
            NextState nextState = processRecursive(currentStateIdentifier, nextStateIdentifier, currentState, state, variables, businessId);
            nextStates.add(nextState);
        });
        return new ComplexProcessResult(result.isCompleted(), nextStates);
    }

    /**
     * 递归处理流程节点，找到所有任务节点或子流程中的任务节点
     *
     * @param currentStateIdentifier
     * @param nextStateIdentifier
     * @param currentState
     * @param nextState
     * @param variables
     * @param businessId
     * @return
     */
    private NextState processRecursive(StateIdentifier currentStateIdentifier, StateIdentifier nextStateIdentifier,
                                       State currentState, State nextState, Map<String, Object> variables, String businessId) {
        if (nextState.isSubProcessState()) {
            String subProcessIdentifier = nextState.getSubProcessIdentifier();
            ProcessInstance subProcessInstance = getProcessInstance(subProcessIdentifier);
            String subProcessStartStateId = subProcessInstance.getProcessDefinition().getStartStateId();
            State subProcessStartState = getState(subProcessIdentifier, subProcessStartStateId);
            return processRecursive(
                    currentStateIdentifier,
                    StateIdentifier.build(
                            nextStateIdentifier.getFullPaths(), nextState.getSubProcessIdentifier(), subProcessStartStateId
                    ),
                    currentState, subProcessStartState, variables, businessId);
        }
        return new NextState(nextStateIdentifier.getFullPaths(), nextState);
    }

    private ProcessInstance getProcessInstance(String identifier) {
        ProcessInstance instance = instanceMap.get(identifier);
        if (instance == null) {
            throw new ProcessException("未找到流程定义，processId: " + identifier);
        }
        return instance;
    }

    private State getState(String identifier, String stateId) {
        ProcessInstance instance = getProcessInstance(identifier);
        return instance.getProcessDefinition().getState(stateId);
    }

    private ProcessResult process(String identifier, String currentStateId, Map<String, Object> variables, String businessId) {
        ProcessInstance instance = getProcessInstance(identifier);
        return instance.process(currentStateId, variables);
    }

}
