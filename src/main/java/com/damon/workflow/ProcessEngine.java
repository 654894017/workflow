package com.damon.workflow;

import com.damon.workflow.condition_parser.IProcessRollback;
import com.damon.workflow.config.NextState;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.config.StateIdentifier;
import com.damon.workflow.evaluator.DefaultEvaluator;
import com.damon.workflow.evaluator.IEvaluator;
import com.damon.workflow.exception.ProcessException;
import com.damon.workflow.utils.CollUtils;
import com.damon.workflow.utils.classpath.ClasspathFlowFileLoader;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ProcessEngine {

    private Map<String, ProcessInstance> instanceMap = new ConcurrentHashMap<>();
    private IEvaluator evaluator;
    private List<String> flowYamlFiles;
    private IProcessRollback processRollback;

    private ProcessEngine(Builder builder) {
        this.evaluator = builder.evaluator;
        this.flowYamlFiles = builder.flowYamlFiles;
        this.processRollback = builder.processRollback;
        ClasspathFlowFileLoader loader = new ClasspathFlowFileLoader();
        List<String> flows = loader.loadFilesFromFlowFolder();
        if (CollUtils.isNotEmpty(flowYamlFiles)) {
            flowYamlFiles.forEach(flowYamlFile -> {
                String processIdentifier = registerProcessInstance(ProcessInstance.loadYaml(
                        flowYamlFile, this.evaluator == null ? DefaultEvaluator.build() : builder.evaluator)
                );
                log.info("register process: [{}] succeeded", processIdentifier);
            });
        }
        flows.forEach(content -> {
            String processIdentifier = registerProcessInstance(ProcessInstance.load(
                    content, this.evaluator == null ? DefaultEvaluator.build() : builder.evaluator)
            );
            log.info("register process: [{}] succeeded", processIdentifier);
        });
    }

    private String registerProcessInstance(ProcessInstance instance) {
        String identifier = instance.getProcessDefinition().getIdentifier();
        if (instanceMap.containsKey(instance.getProcessDefinition().getIdentifier())) {
            throw new ProcessException("流程定义ID重复定义，请检查配置文件，processId: " + identifier);
        }
        instanceMap.put(identifier, instance);
        return identifier;
    }

    public ComplexProcessResult rollback(StateIdentifier currentStateIdentifier, String businessId) {
        if (processRollback == null) {
            throw new ProcessException("流程回退功能未配置");
        }
        return processRollback.rollback(currentStateIdentifier, businessId);
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
        return process(StateIdentifier.buildFromIdentifiers(processIdentifier, processDefinition.getStartStateId()), variables, businessId);
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
        result.setStateProcesResult(variables.get(ProcessConstant.STATE_PROCESS_RESULT));
        return result;
    }

    private ComplexProcessResult doProcess(StateIdentifier currentStateIdentifier, Map<String, Object> variables, String businessId) {
        ProcessResult result = process(
                currentStateIdentifier.getCurrentStateProcessIdentifier(),
                currentStateIdentifier.getCurrentStateId(), variables, businessId
        );
        if (result.isCompleted() && currentStateIdentifier.isSubProcess()) {
            State subProcessState = getState(currentStateIdentifier.getParentProcessIdentifier(), currentStateIdentifier.getSubProcessStateId());
            return doProcess(
                    StateIdentifier.buildFromFullPaths(currentStateIdentifier.getParentProcessFullPaths(), subProcessState.getNextStateId()),
                    variables, businessId
            );
        }
        List<NextState> nextStates = new ArrayList<>();
        result.getNextStates().forEach(state -> {
            StateIdentifier stateIdentifier = StateIdentifier.buildFromFullPaths(currentStateIdentifier.getFullPathsExcludingLast(), state.getId());
            if (state.isSubProcessState()) {
                nextStates.add(getSubProcessStartState(stateIdentifier, state));
            } else {
                nextStates.add(new NextState(stateIdentifier.getFullPaths(), state));
            }
        });
        return new ComplexProcessResult(result.isCompleted(), nextStates);
    }

    /**
     * @param stateIdentifier
     * @param state
     * @return
     */
    private NextState getSubProcessStartState(StateIdentifier stateIdentifier, State state) {
        String subProcessIdentifier = state.getSubProcessIdentifier();
        ProcessInstance subProcessInstance = getProcessInstance(subProcessIdentifier);
        String subProcessStartStateId = subProcessInstance.getProcessDefinition().getStartStateId();
        State subProcessStartState = getState(subProcessIdentifier, subProcessStartStateId);
        return new NextState(
                StateIdentifier.buildFromFullPaths(stateIdentifier.getFullPaths(), state.getSubProcessIdentifier(), subProcessStartStateId).getFullPaths(),
                subProcessStartState
        );
    }

    private ProcessInstance getProcessInstance(String identifier) {
        ProcessInstance instance = instanceMap.get(identifier);
        if (instance == null) {
            throw new ProcessException("未找到流程定义，processId: " + identifier);
        }
        return instance;
    }

    /**
     * 获取流程步骤的描述信息
     *
     * @param identifier
     * @param stateId
     * @return
     */
    public State getState(String identifier, String stateId) {
        ProcessInstance instance = getProcessInstance(identifier);
        return instance.getProcessDefinition().getState(stateId);
    }

    private ProcessResult process(String identifier, String currentStateId, Map<String, Object> variables, String businessId) {
        ProcessInstance instance = getProcessInstance(identifier);
        return instance.process(currentStateId, variables);
    }

    public static class Builder {
        private IEvaluator evaluator;
        private List<String> flowYamlFiles;
        private IProcessRollback processRollback;

        public Builder evaluator(IEvaluator evaluator) {
            this.evaluator = evaluator;
            return this;
        }

        public Builder processRollback(IProcessRollback processRollback) {
            this.processRollback = processRollback;
            return this;
        }

        public Builder flowYamlFiles(String... flowYamlFiles) {
            this.flowYamlFiles = Stream.of(flowYamlFiles).collect(Collectors.toList());
            return this;
        }

        public ProcessEngine build() {
            return new ProcessEngine(this);
        }
    }

}
