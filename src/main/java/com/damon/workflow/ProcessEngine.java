package com.damon.workflow;

import com.damon.workflow.config.NextState;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.config.StateIdentifier;
import com.damon.workflow.evaluator.DefaultEvaluator;
import com.damon.workflow.evaluator.IEvaluator;
import com.damon.workflow.exception.ProcessException;
import com.damon.workflow.handler.IProcessRejectionHandler;
import com.damon.workflow.handler.IProcessWithdrawHandler;
import com.damon.workflow.utils.CollUtils;
import com.damon.workflow.utils.classpath.ClasspathFlowFileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ProcessEngine {
    private final Logger log = LoggerFactory.getLogger(ProcessEngine.class);

    private Map<String, ProcessInstance> instanceMap = new ConcurrentHashMap<>();
    private IEvaluator evaluator;
    private List<String> flowYamlFiles;

    private IProcessRejectionHandler processRejectionHandler;
    private IProcessWithdrawHandler processWithdrawHandler;

    protected ProcessEngine(Builder builder) {
        this.evaluator = builder.evaluator;
        this.flowYamlFiles = builder.flowYamlFiles;
        this.processRejectionHandler = builder.rejectionHandler;
        this.processWithdrawHandler = builder.withdrawHandler;
        loadWorkflow();
    }

    private void loadWorkflow() {
        ClasspathFlowFileLoader loader = new ClasspathFlowFileLoader();
        List<String> flows = loader.loadFilesFromFlowFolder();
        flows.forEach(content -> {
            String processIdentifier = registerProcessInstance(
                    ProcessInstance.load(
                            content,
                            this.evaluator == null ? DefaultEvaluator.build() : this.evaluator
                    )
            );
            log.info("register process: [{}] succeeded", processIdentifier);
        });
        if (CollUtils.isNotEmpty(flowYamlFiles)) {
            flowYamlFiles.forEach(flowYamlFile -> {
                String processIdentifier = registerProcessInstance(
                        ProcessInstance.loadYaml(
                                flowYamlFile,
                                this.evaluator == null ? DefaultEvaluator.build() : this.evaluator
                        )
                );
                log.info("register process: [{}] succeeded", processIdentifier);
            });
        }
    }

    private String registerProcessInstance(ProcessInstance instance) {
        String identifier = instance.getProcessDefinition().getIdentifier();
        if (instanceMap.containsKey(instance.getProcessDefinition().getIdentifier())) {
            throw new ProcessException("流程定义ID重复定义，请检查配置文件，processId: " + identifier);
        }
        instanceMap.put(identifier, instance);
        return identifier;
    }

    /**
     * 撤回
     *
     * @return
     */
    public StateIdentifier withdraw(
            StateIdentifier currentStateIdentifier,
            Map<String, Object> variables,
            String businessId
    ) {
        if (this.processWithdrawHandler == null) {
            throw new ProcessException("流程撤回功能未配置");
        }
        ProcessInstance instance = this.getProcessInstance(
                currentStateIdentifier.getCurrentStateProcessIdentifier()
        );
        RuntimeContext context = new RuntimeContext(
                instance.getProcessDefinition(),
                currentStateIdentifier,
                variables,
                businessId
        );
        return this.processWithdrawHandler.withdraw(context);
    }

    /**
     * 驳回
     *
     * @param currentStateIdentifier
     * @param businessId
     * @return
     */
    public List<StateIdentifier> reject(
            StateIdentifier currentStateIdentifier,
            Map<String, Object> variables,
            String businessId
    ) {
        if (this.processRejectionHandler == null) {
            throw new ProcessException("流程回退功能未配置");
        }
        ProcessInstance instance = this.getProcessInstance(
                currentStateIdentifier.getCurrentStateProcessIdentifier()
        );
        RuntimeContext context = new RuntimeContext(
                instance.getProcessDefinition(),
                currentStateIdentifier,
                variables,
                businessId
        );
        return this.processRejectionHandler.reject(context);
    }


    /**
     * 开启流程
     *
     * @param processIdentifier
     * @param variables
     * @return
     */
    public ComplexProcessResult process(
            String processIdentifier,
            Map<String, Object> variables
    ) {
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
    public ComplexProcessResult process(
            String processIdentifier,
            Map<String, Object> variables,
            String businessId
    ) {
        ProcessInstance instance = getProcessInstance(processIdentifier);
        ProcessDefinition processDefinition = instance.getProcessDefinition();
        return process(StateIdentifier.buildFromIdentifiers(processIdentifier, processDefinition.getStartStateId()), variables, businessId);
    }

    /**
     * @param currentStateIdentifier
     * @param variables
     * @return
     */
    public ComplexProcessResult process(
            StateIdentifier currentStateIdentifier,
            Map<String, Object> variables
    ) {
        return process(currentStateIdentifier, variables, null);
    }

    /**
     * @param currentStateIdentifier
     * @param variables
     * @param businessId
     * @return
     */
    public ComplexProcessResult process(
            StateIdentifier currentStateIdentifier,
            Map<String, Object> variables,
            String businessId
    ) {
        ComplexProcessResult result = processRecursive(currentStateIdentifier, variables, businessId);
        result.setCurrentStateIdentifier(currentStateIdentifier);
        result.setResult(variables.get(ProcessConstant.STATE_PROCESS_RESULT));
        return result;
    }

    private ComplexProcessResult processRecursive(
            StateIdentifier currentStateIdentifier,
            Map<String, Object> variables,
            String businessId
    ) {
        ProcessInstance instance = getProcessInstance(
                currentStateIdentifier.getCurrentStateProcessIdentifier()
        );
        ProcessResult result = instance.process(
                currentStateIdentifier.getCurrentStateId(),
                variables,
                businessId
        );
        if (result.isCompleted() && currentStateIdentifier.isSubProcess()) {
            //如果子流程执行完成，则找到这个父流程的的子流程节点，继续执行下一个节点任务
            State subProcessState = getState(
                    currentStateIdentifier.getParentProcessIdentifier(),
                    currentStateIdentifier.getSubProcessStateId()
            );
            return processRecursive(
                    StateIdentifier.buildFromFullPaths(
                            currentStateIdentifier.getParentProcessFullPaths(),
                            subProcessState.getNextStateId()
                    ),
                    variables,
                    businessId
            );
        }
        List<NextState> nextStates = new ArrayList<>();
        result.getNextStates().forEach(state -> {
            StateIdentifier stateIdentifier = StateIdentifier.buildFromFullPaths(
                    currentStateIdentifier.getFullPathsExcludingLast(),
                    state.getId()
            );
            if (state.isSubProcessState()) {
                nextStates.add(getSubProcessStartState(stateIdentifier, state));
            } else {
                nextStates.add(new NextState(stateIdentifier.getFullPaths(), state));
            }
        });
        return new ComplexProcessResult(result.isCompleted(), nextStates);
    }

    /**
     * 获取子流程开始节点
     *
     * @param stateIdentifier
     * @param state
     * @return
     */
    private NextState getSubProcessStartState(StateIdentifier stateIdentifier, State state) {
        String subProcessIdentifier = state.getSubProcessIdentifier();
        ProcessInstance subProcessInstance = getProcessInstance(subProcessIdentifier);
        //获取子流程ID
        String subProcessStartStateId = subProcessInstance.getProcessDefinition().getStartStateId();
        //获取子流程开启节点
        State subProcessStartState = getState(subProcessIdentifier, subProcessStartStateId);
        return new NextState(
                StateIdentifier.buildFromFullPaths(
                        stateIdentifier.getFullPaths(),
                        state.getSubProcessIdentifier(),
                        subProcessStartStateId
                ).getFullPaths(),
                subProcessStartState
        );
    }

    public ProcessInstance getProcessInstance(String identifier) {
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


    public static class Builder {
        protected IEvaluator evaluator;
        protected List<String> flowYamlFiles;
        private IProcessRejectionHandler rejectionHandler;
        private IProcessWithdrawHandler withdrawHandler;


        public Builder evaluator(IEvaluator evaluator) {
            this.evaluator = evaluator;
            return this;
        }


        public Builder flowYamlFiles(String... flowYamlFiles) {
            this.flowYamlFiles = Stream.of(flowYamlFiles).collect(Collectors.toList());
            return this;
        }


        public Builder withdrawHandler(IProcessWithdrawHandler withdrawHandler) {
            this.withdrawHandler = withdrawHandler;
            return this;
        }

        public Builder rejectionHandler(IProcessRejectionHandler rejectionHandler) {
            this.rejectionHandler = rejectionHandler;
            return this;
        }

        public ProcessEngine build() {
            return new ProcessEngine(this);
        }


    }

}
