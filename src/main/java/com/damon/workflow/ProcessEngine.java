package com.damon.workflow;

import com.damon.workflow.config.NextState;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.config.StateIdentifier;
import com.damon.workflow.exception.ProcessException;
import com.damon.workflow.utils.ClasspathFileUtils;

import java.util.ArrayList;
import java.util.List;
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

    public ComplexProcessResult process(String identifier, Map<String, Object> variables, String businessId) {
        ProcessInstance instance = getProcessInstance(identifier);
        ProcessDefinition processDefinition = instance.getProcessDefinition();
        return process(new StateIdentifier(identifier, processDefinition.getStartStateId()), variables, businessId);
    }

    public ComplexProcessResult process(StateIdentifier currentStateIdentifier, Map<String, Object> variables, String businessId) {
        ProcessResult result = process(currentStateIdentifier.getCurrentStateProcessIdentifier(), currentStateIdentifier.getCurrentStateId(), variables, businessId);
        if (result.isCompleted() && currentStateIdentifier.isSubProcess()) {
            State subProcessState = getState(currentStateIdentifier.getParentProcessIdentifier(), currentStateIdentifier.getSubProcessStateId());
            return process(
                    new StateIdentifier(
                            currentStateIdentifier.getParentProcessIdentifier(),
                            subProcessState.getNextStateId()
                    ),
                    variables,
                    businessId
            );
        }
        State currentState = getState(currentStateIdentifier.getCurrentStateProcessIdentifier(), currentStateIdentifier.getCurrentStateId());
        List<NextState> states = new ArrayList<>();
        result.getNextStates().forEach(state -> {
            StateIdentifier nextStateIdentifier = new StateIdentifier(
                    currentStateIdentifier.getFullPathsExcludingLast(),
                    state.getId()
            );
            NextState nextState = processRecursive(currentStateIdentifier, nextStateIdentifier, currentState, state, variables, businessId);
            states.add(nextState);
        });
        return new ComplexProcessResult(result.isCompleted(), states);
    }

    /**
     * 递归处理流程节点，找到所有任务节点或子流程中的任务节点
     *
     * @param nextStateIdentifier 主流程ID
     * @param currentState        当前状态
     * @param variables           流程变量
     * @param businessId          业务ID
     * @return 后续任务节点或子流程任务节点
     */
    private NextState processRecursive(StateIdentifier currentStateIdentifier, StateIdentifier nextStateIdentifier,
                                       State currentState, State nextState, Map<String, Object> variables, String businessId) {
        String nextStateType = nextState.getType();
        if (ProcessConstant.isSubProcessState(nextStateType)) {
            // 当前节点是子流程，递归处理子流程
            String subProcessIdentifier = nextState.getSubProcessIdentifier();
            ProcessInstance subInstance = getProcessInstance(subProcessIdentifier);
            String subProcessStartStateId = subInstance.getProcessDefinition().getStartStateId();
            State subProcessStartState = getState(subProcessIdentifier, subProcessStartStateId);
            return processRecursive(
                    currentStateIdentifier,
                    new StateIdentifier(
                            nextStateIdentifier.getCurrentStateProcessIdentifier(),
                            nextState.getId(),
                            nextState.getSubProcessIdentifier(),
                            subProcessStartStateId
                    ),
                    currentState, subProcessStartState, variables, businessId);
        }
        return new NextState(
                currentStateIdentifier.getFullPaths(), currentState,
                nextStateIdentifier.getFullPaths(), nextState
        );
    }

    public ProcessInstance getProcessInstance(String identifier) {
        ProcessInstance instance = instanceMap.get(identifier);
        if (instance == null) {
            throw new ProcessException("未找到流程定义，processId: " + identifier);
        }
        return instance;
    }

    public State getState(String identifier, String stateId) {
        ProcessInstance instance = getProcessInstance(identifier);
        return instance.getProcessDefinition().getState(stateId);
    }

    public ProcessResult process(String identifier, Map<String, Object> variables) {
        ProcessInstance instance = instanceMap.get(identifier);
        return process(identifier, instance.getProcessDefinition().getStartStateId(), variables, null);
    }

    public ProcessResult process(String identifier, String currentStateId, Map<String, Object> variables, String businessId) {
        ProcessInstance instance = instanceMap.get(identifier);
        return instance.process(currentStateId, variables);
    }


}
