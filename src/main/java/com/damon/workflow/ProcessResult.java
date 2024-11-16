package com.damon.workflow;


import com.damon.workflow.config.State;
import com.damon.workflow.exception.ProcessException;
import com.damon.workflow.utils.CollUtils;

import java.util.List;

public class ProcessResult {
    private String processId;
    private boolean completed;
    private State currentState;
    private List<State> nextStates;
    private Object result;

    public ProcessResult(boolean completed, State currentState, List<State> nextStates, Object result) {
        this.completed = completed;
        this.nextStates = nextStates;
        this.currentState = currentState;
        this.result = result;
    }


    public ProcessResult(String processId, State currentState, List<State> nextStates, Object result) {
        this.nextStates = nextStates;
        this.currentState = currentState;
        this.result = result;
    }

    public boolean isCompleted() {
        if (CollUtils.isEmpty(nextStates)) {
            throw new ProcessException("流程ID: " + processId + ", 任务ID: " + currentState.getId() + ", 异常结束,请确认流程设计是否正确");
        }
        return ProcessConstant.END.equals(nextStates.get(0).getType());
    }

    public State getCurrentState() {
        return currentState;
    }

    public List<State> getNextStates() {
        return nextStates;
    }

    public <T> T getResult() {
        return (T) result;
    }
}
