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
import com.damon.workflow.utils.YamlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProcessInstance {
    private final CaseInsensitiveMap<ITask> taskMap = new CaseInsensitiveMap<>();
    private final CaseInsensitiveMap<IGateway> gatewayMap = new CaseInsensitiveMap<>();
    private final CaseInsensitiveMap<IEvaluator> evaluatorMap = new CaseInsensitiveMap<>();
    private final CaseInsensitiveMap<IProcessor> processorsMap = new CaseInsensitiveMap<>();
    private final CaseInsensitiveMap<IConditionParser> conditionsMap = new CaseInsensitiveMap<>();
    private final ProcessDefinition processDefinition;

    private ProcessInstance(String content) {
        taskMap.put(ProcessConstant.USER_TASK, new UserTask(processorsMap, conditionsMap, evaluatorMap));
        taskMap.put(ProcessConstant.START, new StartTask(processorsMap, conditionsMap, evaluatorMap));
        taskMap.put(ProcessConstant.END, new EndTask(processorsMap));
        gatewayMap.put(ProcessConstant.EXCLUSIVE_GATEWAY, new ExclusiveGateway(evaluatorMap, conditionsMap));
        gatewayMap.put(ProcessConstant.PARALLEL_END_GATEWAY, new ParallelEndGateway(evaluatorMap, conditionsMap));
        gatewayMap.put(ProcessConstant.PARALLEL_START_GATEWAY, new ParallelStartGateway(evaluatorMap, conditionsMap));
        evaluatorMap.put(ProcessConstant.DEFAULT_EVALUATOR, new JavaScriptEvaluator());
        ProcessConfig config = YamlUtils.load(content, ProcessConfig.class);
        this.processDefinition = config.getProcessDefinition();
    }

    public static ProcessInstance load(String content) {
        return new ProcessInstance(content);
    }

    public static ProcessInstance loadYaml(String classpathYamlFile) {
        String content = ClasspathFileUtils.readFileAsString(classpathYamlFile);
        return load(content);
    }

    public void registerConditionParsers(IConditionParser... parsers) {
        for (IConditionParser parser : parsers) {
            if (conditionsMap.containsKey(parser.getClass().getName())) {
                throw new ProcessException("ConditionParser name 重复定义，请检查配置文件，name: " + parser.getClass().getName());
            }
            conditionsMap.put(parser.getClass().getName(), parser);
        }
    }

    public void registerProcessors(IProcessor... processors) {
        for (IProcessor processor : processors) {
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
        return process(processDefinition.getStartState(), variables);
    }

    /**
     * 流程处理
     *
     * @param currentStateId
     * @param variables
     * @return
     */
    public ProcessResult process(String currentStateId, Map<String, Object> variables) {
        // 验证流程配置
        if (processDefinition == null) {
            throw new ProcessException("流程ID: " + processDefinition.getIdentifier() + ", 未配置流程信息");
        }

        // 验证当前状态
        State currentState = processDefinition.getState(currentStateId);
        if (currentState == null) {
            throw new ProcessException("无效的流程状态ID: " + currentStateId);
        }

        // 获取任务执行器
        ITask task = taskMap.get(currentState.getType());
        if (task == null) {
            throw new ProcessException("未找到任务类型: " + currentState.getType());
        }

        // 执行任务
        RuntimeContext context = new RuntimeContext(processDefinition, currentState, variables);
        Set<State> taskNextStates = task.execute(context);

        // 后续节点处理
        List<State> nextStates = new ArrayList<>();
        for (State nextState : taskNextStates) {
            // 当前节点等于后续节点说明当前节点的条件不足以满足到后续节点
            if (currentState.equals(nextState)) {
                nextStates.add(nextState);
                continue;
            }
            // 如果是非任务节点，递归查找其后续节点
            if (!ProcessConstant.isTaskState(nextState.getType())) {
                nextStates.addAll(findNextStates(processDefinition, nextState, context));
            } else {
                // 直接添加任务节点
                nextStates.add(nextState);
            }
        }

        // 检查流程是否完成
        boolean isCompleted = isCompleted(nextStates, currentState);
        if (isCompleted) {
            ITask endTask = taskMap.get(nextStates.get(0).getType());
            endTask.execute(context);
        }
        return new ProcessResult(isCompleted, processDefinition.getIdentifier(), currentState, nextStates);
    }

    public boolean isCompleted(List<State> nextStates, State currentState) {
        if (CollUtils.isEmpty(nextStates)) {
            throw new ProcessException("流程ID: " + processDefinition.getIdentifier() + ", 任务ID: " + currentState.getId() + ", 异常结束,请确认流程设计是否正确");
        }
        return ProcessConstant.END.equals(nextStates.get(0).getType());
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
        findNextStatesRecursive(processDefinition, currentState, context, nextStates);
        return nextStates;
    }

    /**
     * 递归查找后续节点
     */
    private void findNextStatesRecursive(ProcessDefinition processDefinition, State currentState,
                                         RuntimeContext context, List<State> result) {
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
        } else {
            if (ProcessConstant.isTaskState(currentType)) {
                result.add(currentState);
            } else {
                if (currentState.getNextState() != null) {
                    State nextState = processDefinition.getState(currentState.getNextState());
                    findNextStatesRecursive(processDefinition, nextState, context, result);
                }
            }
        }
    }


    private void handleParallelEndGateway(ProcessDefinition processDefinition, State gatewayState, RuntimeContext context, List<State> result) {
        IGateway gateway = gatewayMap.get(gatewayState.getType());
        Set<State> nextStates = gateway.execute(new RuntimeContext(processDefinition, gatewayState, context.getVariables()));
        nextStates.forEach(state -> {
            if (gatewayState == state) {
                result.add(gatewayState);
            } else {
                findNextStatesRecursive(processDefinition, state, context, result);
            }
        });
    }

    private void handleGateway(ProcessDefinition processDefinition, State gatewayState, RuntimeContext context, List<State> result) {
        IGateway gateway = gatewayMap.get(gatewayState.getType());
        Set<State> nextStates = gateway.execute(new RuntimeContext(processDefinition, gatewayState, context.getVariables()));
        nextStates.forEach(state -> {
            findNextStatesRecursive(processDefinition, state, context, result);
        });
    }

    public ProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public String getProcessIdentifier() {
        return processDefinition.getIdentifier();
    }
}