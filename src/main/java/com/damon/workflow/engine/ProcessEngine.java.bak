package com.damon.workflow.engine;

import com.damon.workflow.*;
import com.damon.workflow.config.NextState;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.config.StateIdentifier;
import com.damon.workflow.evaluator.DefaultEvaluator;
import com.damon.workflow.evaluator.IEvaluator;
import com.damon.workflow.exception.ProcessException;
import com.damon.workflow.process.IProcessor;
import com.damon.workflow.spring.ApplicationContextHelper;
import com.damon.workflow.utils.CollUtils;
import com.damon.workflow.utils.classpath.ClasspathFlowFileLoader;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 默认事务流程引擎
 * <p>
 * 适用于:时间跨度短（通常是毫秒到秒级别），事务的范围局限在单个数据库或服务中
 */
@Slf4j
public class ProcessEngine {

    private Map<String, ProcessInstance> instanceMap = new ConcurrentHashMap<>();
    private IEvaluator evaluator;
    private List<String> flowYamlFiles;
    private ExecutorService executorService;

    protected ProcessEngine(Builder builder) {
        this.evaluator = builder.evaluator;
        this.flowYamlFiles = builder.flowYamlFiles;
        this.executorService = builder.executorService;
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

    public ComplexProcessResult trigger(String processIdentifier, Map<String, Object> variables) {
        return this.trigger(processIdentifier, variables, null);
    }

    /**
     * 触发实时流程
     *
     * @param processIdentifier
     * @param variables
     * @return
     */
    public ComplexProcessResult trigger(String processIdentifier, Map<String, Object> variables, String businessId) {
        ComplexProcessResult result = process(processIdentifier, variables, businessId);
        boolean isCompleted = result.isCompleted();
        Stack<ComplexProcessResult> resultStack = new Stack<>();
        resultStack.push(result);
        while (!resultStack.isEmpty() && !isCompleted) {
            List<NextState> nextStates = resultStack.pop().getNextStates();
            if (nextStates.size() == 1) {
                StateIdentifier stateIdentifier = StateIdentifier.buildFromFullPaths(nextStates.get(0).getNextStateFullPaths());
                ComplexProcessResult processResult = process(stateIdentifier, variables, businessId);
                resultStack.push(processResult);
                isCompleted = processResult.isCompleted();
            } else {
                List<CompletableFuture<ComplexProcessResult>> resultFutures = nextStates.stream().map(nextState -> {
                    return CompletableFuture.supplyAsync(
                            () -> process(StateIdentifier.buildFromFullPaths(nextState.getNextStateFullPaths()), variables),
                            executorService
                    );
                }).collect(Collectors.toList());
                for (CompletableFuture<ComplexProcessResult> resultFuture : resultFutures) {
                    ComplexProcessResult processResult = resultFuture.join();
                    if (!processResult.isCompleted()) {
                        resultStack.push(processResult);
                    }
                }
            }
        }
        result.setStatesProcessResult((Map<String, Object>) variables.get(ProcessConstant.STATES_PROCESS_RESULT));
        return result;
    }


    /**
     * 开启流程
     *
     * @param processIdentifier
     * @param variables
     * @return
     */
    protected ComplexProcessResult process(String processIdentifier, Map<String, Object> variables) {
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
    protected ComplexProcessResult process(String processIdentifier, Map<String, Object> variables, String businessId) {
        ProcessInstance instance = getProcessInstance(processIdentifier);
        ProcessDefinition processDefinition = instance.getProcessDefinition();
        return process(StateIdentifier.buildFromIdentifiers(processIdentifier, processDefinition.getStartStateId()), variables, businessId);
    }

    /**
     * @param currentStateIdentifier
     * @param variables
     * @return
     */
    protected ComplexProcessResult process(StateIdentifier currentStateIdentifier, Map<String, Object> variables) {
        return process(currentStateIdentifier, variables, null);
    }

    /**
     * @param currentStateIdentifier
     * @param variables
     * @param businessId
     * @return
     */
    protected ComplexProcessResult process(StateIdentifier currentStateIdentifier, Map<String, Object> variables, String businessId) {
        ComplexProcessResult result = doProcess(currentStateIdentifier, variables, businessId);
        result.setCurrentStateIdentifier(currentStateIdentifier);
        result.setStateProcessResult(variables.get(ProcessConstant.STATE_PROCESS_RESULT));
        State state = this.getState(currentStateIdentifier.getCurrentStateProcessIdentifier(), currentStateIdentifier.getCurrentStateId());
        List<String> classProcessors = state.getProcessors();
        ProcessInstance instance = this.getProcessInstance(currentStateIdentifier.getCurrentStateProcessIdentifier());
        RuntimeContext context = new RuntimeContext(instance.getProcessDefinition(), currentStateIdentifier, variables, businessId);
        if (CollUtils.isNotEmpty(classProcessors)) {
            classProcessors.forEach(classProcessor -> {
                IProcessor processor = ApplicationContextHelper.getBean(classProcessor);
                if (processor.isMatch(context)) {
                    processor.postProcess(result, context);
                }
            });
        }
        return result;
    }

    private ComplexProcessResult doProcess(StateIdentifier currentStateIdentifier, Map<String, Object> variables, String businessId) {
        ProcessInstance instance = getProcessInstance(currentStateIdentifier.getCurrentStateProcessIdentifier());
        ProcessResult result = instance.process(currentStateIdentifier.getCurrentStateId(), variables, businessId);
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


    public static class Builder<T extends Builder<T>> {
        protected ExecutorService executorService;
        protected IEvaluator evaluator;
        protected List<String> flowYamlFiles;

        public T evaluator(IEvaluator evaluator) {
            this.evaluator = evaluator;
            return (T) this;
        }

        public T executorService(ExecutorService executorService) {
            this.executorService = executorService;
            return (T) this;
        }

        public T flowYamlFiles(String... flowYamlFiles) {
            this.flowYamlFiles = Stream.of(flowYamlFiles).collect(Collectors.toList());
            return (T) this;
        }

        public ProcessEngine build() {
            return new ProcessEngine(this);
        }


    }

}
