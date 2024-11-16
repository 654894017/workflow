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
import com.damon.workflow.utils.YamlUtils;

import java.util.*;

public class ProcessEngine {
    private final CaseInsensitiveMap<ITask> globalTask = new CaseInsensitiveMap<>();
    private final CaseInsensitiveMap<IGateway> globalGateway = new CaseInsensitiveMap<>();
    private final CaseInsensitiveMap<IEvaluator> evaluatorMap = new CaseInsensitiveMap<>();
    private final CaseInsensitiveMap<IProcessor> processorsMap = new CaseInsensitiveMap<>();
    private final CaseInsensitiveMap<IConditionParser> conditionsMap = new CaseInsensitiveMap<>();
    private final ProcessConfig config;
    private final String processId;

    public ProcessEngine(String content) {
        globalTask.put(ProcessConstant.USER_TASK, new UserTask());
        globalTask.put(ProcessConstant.START, new StartTask());
        globalTask.put(ProcessConstant.END, new EndTask());
        globalGateway.put(ProcessConstant.EXCLUSIVE_GATEWAY, new ExclusiveGateway(evaluatorMap, conditionsMap));
        globalGateway.put(ProcessConstant.PARALLEL_END_GATEWAY, new ParallelEndGateway(evaluatorMap, conditionsMap));
        globalGateway.put(ProcessConstant.PARALLEL_START_GATEWAY, new ParallelStartGateway(evaluatorMap, conditionsMap));
        evaluatorMap.put(ProcessConstant.DEFAULT_EVALUATOR, new JavaScriptEvaluator());
        this.config = YamlUtils.load(content, ProcessConfig.class);
        this.processId = config.getProcessDefinition().getId();
    }

    public void registerConditionParsers(IConditionParser... parsers) {
        for (IConditionParser parser : parsers) {
            conditionsMap.put(parser.getClass().getName(), parser);
        }
    }

    public void registerTasks(ITask... tasks) {
        Arrays.stream(tasks).forEach(task -> globalTask.put(task.getName(), task));
    }

    public void registerProcessors(IProcessor... processors) {
        for (IProcessor<?> processor : processors) {
            processor.stateIds().forEach(stateId -> {
                if (processorsMap.containsKey(stateId)) {
                    throw new ProcessException("Processor stateId 重复定义，请检查配置文件，stateId: " + stateId);
                }
                processorsMap.put(stateId, processor);
            });
        }
    }


    public void registerEvaluator(IEvaluator evaluator) {
        evaluatorMap.put(evaluator.getName(), evaluator);
    }

    /**
     * 开启流程
     *
     * @param variables
     * @return
     */
    public ProcessResult process(Map<String, Object> variables) {
        return process(config.getProcessDefinition().getStartState(), variables);
    }

    /**
     * 流程处理
     *
     * @param currentStateId
     * @param variables
     * @return
     */
    public ProcessResult process(String currentStateId, Map<String, Object> variables) {
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
        return Optional.ofNullable(processorsMap.get(currentStateId))
                .map(processor -> processor.process(context))
                .orElse(null);
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