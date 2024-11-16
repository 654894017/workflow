package com.damon.workflow;


import com.damon.workflow.config.ProcessConfig;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.evaluator.IEvaluator;
import com.damon.workflow.evaluator.JavaScriptEvaluator;
import com.damon.workflow.exception.ProcessException;
import com.damon.workflow.gateway.ExclusiveGateway;
import com.damon.workflow.gateway.IGateway;
import com.damon.workflow.gateway.ParallelEndGateway;
import com.damon.workflow.gateway.ParallelStartGateway;
import com.damon.workflow.task.EndTask;
import com.damon.workflow.task.ITask;
import com.damon.workflow.task.StartTask;
import com.damon.workflow.task.UserTask;
import com.damon.workflow.utils.CaseInsensitiveMap;
import com.damon.workflow.utils.ClasspathFileUtils;
import com.damon.workflow.utils.CollUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.util.*;

public class ProcessEngine {
    private final CaseInsensitiveMap<ProcessConfig> configs = new CaseInsensitiveMap<>();
    private final CaseInsensitiveMap<ITask> globalTask = new CaseInsensitiveMap<>();
    private final CaseInsensitiveMap<IGateway> globalGateway = new CaseInsensitiveMap<>();
    private final CaseInsensitiveMap<IEvaluator> evaluatorMap = new CaseInsensitiveMap<>();
    private final CaseInsensitiveMap<IProcesDefinitionCallback> processDefinitionCallMap = new CaseInsensitiveMap<>();
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private final CaseInsensitiveMap<IProcessor> processorsMap = new CaseInsensitiveMap<>();
    private final CaseInsensitiveMap<IConditionParser> conditionsMap = new CaseInsensitiveMap<>();

    public ProcessEngine() {
        globalTask.put(ProcessConstant.USER_TASK, new UserTask());
        globalTask.put(ProcessConstant.START, new StartTask());
        globalTask.put(ProcessConstant.END, new EndTask());
        globalGateway.put(ProcessConstant.EXCLUSIVE_GATEWAY, new ExclusiveGateway(evaluatorMap, conditionsMap));
        globalGateway.put(ProcessConstant.PARALLEL_END_GATEWAY, new ParallelEndGateway(evaluatorMap, conditionsMap));
        globalGateway.put(ProcessConstant.PARALLEL_START_GATEWAY, new ParallelStartGateway(evaluatorMap, conditionsMap));
        evaluatorMap.put(ProcessConstant.DEFAULT_EVALUATOR, new JavaScriptEvaluator());
    }

    public void registerConditionParsers(String processId, IConditionParser... parsers) {
        for (IConditionParser parser : parsers) {
            String processStateId = processId + ":" + parser.getClass().getName();
            conditionsMap.put(processStateId, parser);
        }
    }

    public void registerTasks(ITask... tasks) {
        Arrays.stream(tasks).forEach(task -> globalTask.put(task.getName(), task));
    }

    public void registerProcessors(String processId, IProcessor... processors) {
        for (IProcessor<?> processor : processors) {
            processor.stateIds().forEach(stateId -> {
                String processStateId = processId + ":" + stateId;
                processorsMap.put(processStateId, processor);
            });
        }
    }


    public void registerEvaluator(IEvaluator evaluator) {
        evaluatorMap.put(evaluator.getName(), evaluator);
    }


    public void registerProcess(String processId, String content) {
        try {
            ProcessConfig config = mapper.readValue(content, ProcessConfig.class);
            configs.put(processId, config);
        } catch (IOException e) {
            throw new ProcessException("获取流程实例配置异常", e);
        }
    }

    public void registerProcessFromClasspath(String processId, String yamlFile) {
        String content = ClasspathFileUtils.readFileAsString(yamlFile);
        this.registerProcess(processId, content);
    }

    public void registerProcessFromCallback(String processId, IProcesDefinitionCallback callback) {
        String content = callback.callback(processId);
        processDefinitionCallMap.put(processId, callback);
        this.registerProcess(processId, content);
    }

    /**
     * 开启流程
     *
     * @param processId
     * @param variables
     * @return
     */
    public ProcessResult process(String processId, Map<String, Object> variables) {
        ProcessConfig config = configs.get(processId);
        if (config == null) {
            String processDefinition = processDefinitionCallMap.get(processId).callback(processId);
            registerProcess(processId, processDefinition);
        }
        return process(processId, config.getProcessDefinition().getStartState(), variables);
    }

    /**
     * 流程处理
     *
     * @param processId
     * @param currentStateId
     * @param variables
     * @return
     */
    public ProcessResult process(String processId, String currentStateId, Map<String, Object> variables) {
        ProcessConfig config = configs.get(processId);
        if (config == null) {
            throw new ProcessException("无效的流程ID: " + processId);
        }
        ProcessDefinition processDefinition = config.getProcessDefinition();
        if (processDefinition == null) {
            throw new ProcessException("流程ID: " + processId + ", 未配置流程信息");
        }
        State currentState = processDefinition.getState(currentStateId);
        if (currentState == null) {
            throw new ProcessException("无效的流程状态ID: " + currentStateId);
        }
        String currentStateType = currentState.getType();
        ITask task = globalTask.get(currentStateType);
        if (task == null) {
            throw new ProcessException("未找到任务类型: " + currentState.getType());
        }
        RuntimeContext context = new RuntimeContext(processDefinition, currentState, variables);
        task.execute(context);
        Object result = getStateProcessResult(currentStateId, context);
        List<State> nextStatues = findNextStates(processDefinition, currentState, context);
        return new ProcessResult(processId, currentState, nextStatues, result);
    }

    private Object getStateProcessResult(String currentStateId, RuntimeContext context) {
        String processStateId = context.getProcessDefinition().getId() + ":" + currentStateId;
        return Optional.ofNullable(processorsMap.get(processStateId))
                .map(processor -> processor.process(context))
                .orElse(null);
    }

    private boolean isEndedProcess(String processId, State currentState, List<State> nextStatues) {
        if (CollUtils.isNotEmpty(nextStatues) && !nextStatues.get(0).getType().equals(ProcessConstant.END)) {
            return false;
        } else if (CollUtils.isNotEmpty(nextStatues) && nextStatues.get(0).getType().equals(ProcessConstant.END)) {
            return true;
        }
        throw new ProcessException("流程ID: " + processId + ", 任务ID: " + currentState.getId() + ", 异常结束,请确认流程设计是否正确");
    }

    /**
     * 查找当前节点的所有可能后续节点
     *
     * @param processDefinition
     * @param currentState      当前节点ID
     * @param context           执行上下文，包括条件变量等
     * @return 后续所有待执行的节点集合
     */
    public List<State> findNextStates(ProcessDefinition processDefinition, State currentState, RuntimeContext context) {
        List<State> nextStates = new ArrayList<>();
        findNextStatesRecursive(processDefinition, currentState, context, nextStates, true);
        return nextStates;
    }

    /**
     * 递归查找后续节点
     */
    private void findNextStatesRecursive(ProcessDefinition processDefinition, State currentState,
                                         RuntimeContext context, List<State> result, boolean isInitiator) {
        if (currentState == null) {
            return;
        }
        String currentType = currentState.getType();
        if (ProcessConstant.EXCLUSIVE_GATEWAY.equals(currentState.getType())) {
            handleGateway(processDefinition, currentState, context, result);
        } else if (currentType.equals(ProcessConstant.PARALLEL_START_GATEWAY)) {
            handleGateway(processDefinition, currentState, context, result);
        } else if (ProcessConstant.PARALLEL_END_GATEWAY.equals(currentState.getType())) {
            handleParallelEndGateway(processDefinition, currentState, context, result);
        } else if (ProcessConstant.END.equals(currentState.getType())) {
            handleEnd(processDefinition, currentState, context, result);
        } else {
            if (!isInitiator && ProcessConstant.isTaskState(currentType)) {
                result.add(currentState);
            } else {
                if (currentState.getNextState() != null) {
                    State nextState = processDefinition.getState(currentState.getNextState());
                    RuntimeContext rc = new RuntimeContext(processDefinition, nextState, context.getVariables());
                    findNextStatesRecursive(processDefinition, nextState, rc, result, false);
                }
            }
        }
    }

    private void handleEnd(ProcessDefinition processDefinition, State endState, RuntimeContext context, List<State> result) {
        ITask endTask = globalTask.get(endState.getType());
        State nextState = endTask.execute(new RuntimeContext(processDefinition, endState, context.getVariables()));
        result.add(nextState);
    }

    private void handleParallelEndGateway(ProcessDefinition processDefinition, State gatewayState, RuntimeContext context, List<State> result) {
        IGateway gateway = globalGateway.get(gatewayState.getType());
        Set<State> nextStates = gateway.execute(new RuntimeContext(processDefinition, gatewayState, context.getVariables()));
        nextStates.forEach(state -> {
            if (gatewayState == state) {
                result.add(gatewayState);
            } else {
                RuntimeContext nextContext = new RuntimeContext(processDefinition, state, context.getVariables());
                findNextStatesRecursive(processDefinition, state, nextContext, result, false);
            }
        });
    }

    private void handleGateway(ProcessDefinition processDefinition, State gatewayState, RuntimeContext context, List<State> result) {
        IGateway gateway = globalGateway.get(gatewayState.getType());
        Set<State> nextStates = gateway.execute(new RuntimeContext(processDefinition, gatewayState, context.getVariables()));
        nextStates.forEach(state -> {
            RuntimeContext nextContext = new RuntimeContext(processDefinition, state, context.getVariables());
            findNextStatesRecursive(processDefinition, state, nextContext, result, false);
        });
    }
}