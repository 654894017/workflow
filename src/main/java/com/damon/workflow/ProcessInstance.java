package com.damon.workflow;


import com.damon.workflow.config.ProcessConfig;
import com.damon.workflow.config.ProcessDefinition;
import com.damon.workflow.config.State;
import com.damon.workflow.config.StateIdentifier;
import com.damon.workflow.evaluator.DefaultEvaluator;
import com.damon.workflow.evaluator.IEvaluator;
import com.damon.workflow.exception.ProcessException;
import com.damon.workflow.gateway.ExclusiveGateway;
import com.damon.workflow.gateway.IGateway;
import com.damon.workflow.gateway.ParallelEndGateway;
import com.damon.workflow.gateway.ParallelStartGateway;
import com.damon.workflow.task.ITask;
import com.damon.workflow.task.impl.EndTask;
import com.damon.workflow.task.impl.GeneralTask;
import com.damon.workflow.task.impl.StartTask;
import com.damon.workflow.utils.CaseInsensitiveMap;
import com.damon.workflow.utils.CollUtils;
import com.damon.workflow.utils.YamlUtils;
import com.damon.workflow.utils.classpath.ClasspathFileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProcessInstance {
    private final CaseInsensitiveMap<ITask> taskMap = new CaseInsensitiveMap<>();
    private final CaseInsensitiveMap<IGateway> gatewayMap = new CaseInsensitiveMap<>();
    private final ProcessDefinition processDefinition;

    private ProcessInstance(String content, IEvaluator evaluator) {
        taskMap.put(ProcessConstant.GENERAL_TASK, new GeneralTask(evaluator));
        taskMap.put(ProcessConstant.START, new StartTask(evaluator));
        taskMap.put(ProcessConstant.END, new EndTask());
        gatewayMap.put(ProcessConstant.EXCLUSIVE_GATEWAY, new ExclusiveGateway(evaluator));
        gatewayMap.put(ProcessConstant.PARALLEL_END_GATEWAY, new ParallelEndGateway(evaluator));
        gatewayMap.put(ProcessConstant.PARALLEL_START_GATEWAY, new ParallelStartGateway(evaluator));
        ProcessConfig config = YamlUtils.load(content, ProcessConfig.class);
        this.processDefinition = config.getProcessDefinition();

    }

    public static ProcessInstance load(String content, IEvaluator evaluator) {
        return new ProcessInstance(content, DefaultEvaluator.build());
    }

    public static ProcessInstance loadYaml(String classpathYamlFile, IEvaluator evaluator) {
        String content = ClasspathFileUtils.readFileAsString(classpathYamlFile);
        return load(content, evaluator);
    }

    /**
     * 开启流程
     *
     * @param variables
     * @param businessId
     * @return
     */
    public ProcessResult process(Map<String, Object> variables, String businessId) {
        return process(processDefinition.getStartStateId(), variables, businessId);
    }

    /**
     * 开启流程
     *
     * @param variables
     * @return
     */
    public ProcessResult process(Map<String, Object> variables) {
        return process(variables, null);
    }

    /**
     * 流程处理
     *
     * @param currentStateId
     * @param variables
     * @return
     */
    public ProcessResult process(String currentStateId, Map<String, Object> variables) {
        return process(currentStateId, variables, null);
    }

    /**
     * 流程处理
     *
     * @param currentStateId
     * @param variables
     * @param businessId
     * @return
     */
    public ProcessResult process(String currentStateId, Map<String, Object> variables, String businessId) {
        State currentState = processDefinition.getState(currentStateId);
        if (currentState == null) {
            throw new ProcessException("无效的流程状态ID: " + currentStateId);
        }
        RuntimeContext context = new RuntimeContext(
                processDefinition,
                StateIdentifier.buildFromIdentifiers(
                        processDefinition.getIdentifier(),
                        currentStateId
                ),
                variables, businessId
        );
        List<State> taskNextStates;
        if (!currentState.isTaskState()) {
            //非任务节点,那么需要找到这个非任务节点的下一个节点
            taskNextStates = findNextStates(processDefinition, currentState, context);
        } else {
            ITask task = taskMap.get(currentState.getType());
            taskNextStates = task.execute(context);
        }
        // 后续节点处理
        List<State> nextStates = new ArrayList<>();
        for (State nextState : taskNextStates) {
            // 当前节点等于后续节点说明当前节点的条件不足以满足到后续节点
            if (currentState.equals(nextState)) {
                nextStates.add(nextState);
                continue;
            }
            if (nextState.isTaskState() || nextState.isSubProcessState()) {
                // 直接添加任务节点
                nextStates.add(nextState);
            } else {
                // 如果是非任务节点，递归查找其后续节点
                nextStates.addAll(findNextStates(processDefinition, nextState, context));
            }
        }

        // 检查流程是否完成
        boolean isCompleted = isCompleted(nextStates, currentState);
        if (isCompleted) {
            ITask endTask = taskMap.get(nextStates.get(0).getType());
            State state = nextStates.get(0);
            StateIdentifier endStateIdentifier = StateIdentifier.buildFromIdentifiers(
                    processDefinition.getIdentifier(), state.getId()
            );
            endTask.execute(new RuntimeContext(
                    processDefinition,
                    endStateIdentifier,
                    variables,
                    businessId
            ));
        }
        return new ProcessResult(isCompleted, processDefinition.getIdentifier(), currentState, nextStates);
    }

    private boolean isCompleted(List<State> nextStates, State currentState) {
        if (CollUtils.isEmpty(nextStates)) {
            throw new ProcessException("流程ID: " + processDefinition.getIdentifier() + ", 任务ID: " + currentState.getId() + ", 异常结束,请确认流程设计是否正确");
        }
        return currentState.isEndState() || ProcessConstant.END.equals(nextStates.get(0).getType());
    }

    /**
     * 查找当前节点的所有可能后续节点
     *
     * @param processDefinition
     * @param currentState      当前节点ID
     * @param context           执行上下文，包括条件变量等
     * @return 后续所有待执行的节点集合
     */
    private List<State> findNextStates(
            ProcessDefinition processDefinition,
            State currentState,
            RuntimeContext context
    ) {
        List<State> nextStates = new ArrayList<>();
        findNextStatesRecursive(processDefinition, currentState, context, nextStates);
        return nextStates;
    }

    /**
     * 递归查找后续节点
     */
    private void findNextStatesRecursive(
            ProcessDefinition processDefinition,
            State currentState,
            RuntimeContext context,
            List<State> result
    ) {
        if (currentState == null) {
            return;
        }
        if (currentState.isExclusiveGatewayState()) {
            handleGateway(processDefinition, currentState, context, result);
        } else if (currentState.isParallelStartGatewayState()) {
            handleGateway(processDefinition, currentState, context, result);
        } else if (currentState.isParallelEndGatewayState()) {
            handleParallelEndGateway(processDefinition, currentState, context, result);
        } else {
            // 如果找的的步骤是任务节点或者是子流程节点（主流程的），则返回出去。子流程节点的再次转发交由ProcessEngine处理
            if (currentState.isTaskState() || currentState.isSubProcessState()) {
                result.add(currentState);
            }
        }
    }

    private void handleParallelEndGateway(
            ProcessDefinition processDefinition,
            State gatewayState,
            RuntimeContext context,
            List<State> states
    ) {
        IGateway gateway = gatewayMap.get(gatewayState.getType());
        StateIdentifier identifier = StateIdentifier.buildFromIdentifiers(
                processDefinition.getIdentifier(), gatewayState.getId()
        );
        List<State> nextStates = gateway.execute(new RuntimeContext(
                processDefinition,
                identifier,
                context.getVariables(),
                context.getBusinessId()
        ));
        nextStates.forEach(state -> {
            if (gatewayState == state) {
                states.add(state);
            } else {
                findNextStatesRecursive(
                        processDefinition,
                        state,
                        context,
                        states
                );
            }
        });
    }

    private void handleGateway(
            ProcessDefinition processDefinition,
            State gatewayState,
            RuntimeContext context,
            List<State> result
    ) {
        IGateway gateway = gatewayMap.get(gatewayState.getType());
        StateIdentifier identifier = StateIdentifier.buildFromIdentifiers(
                processDefinition.getIdentifier(),
                gatewayState.getId()
        );
        List<State> nextStates = gateway.execute(new RuntimeContext(
                processDefinition,
                identifier,
                context.getVariables(),
                context.getBusinessId()
        ));
        nextStates.forEach(state -> {
            findNextStatesRecursive(
                    processDefinition,
                    state,
                    context,
                    result
            );
        });
    }

    public ProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

//    public State getStateToStateParallelGatewayState(String fromStateId, String toStateId) {
//        State from = processDefinition.getState(fromStateId);
//    }
//
//    private State test(State from, String toStateId) {
//        if (from.isTaskState() && from.getNextStateId().equals(toStateId)) {
//            return null;
//        } else if (from.isTaskState() && !from.getNextStateId().equals(toStateId)) {
//            State state = processDefinition.getState(from.getNextStateId());
//            return test(state, toStateId);
//        } else if (from.isParallelStartGatewayState() &&
//                from.getConditions().stream().map(Condition::getNextStateId).collect(Collectors.toList()).contains(toStateId)) {
//            return from;
//        } else if (from.isParallelStartGatewayState() &&
//                !from.getConditions().stream().map(Condition::getNextStateId).collect(Collectors.toList()).contains(toStateId)) {
//            for (Condition condition : from.getConditions()) {
//                State state = processDefinition.getState(condition.getNextStateId());
//                State gatewayState = test(state, toStateId);
//                if (gatewayState == null) {
//                    continue;
//                }
//                return gatewayState;
//            }
//        }
//        return null;
//    }


}