package com.damon.workflow.engine;

import com.damon.workflow.ComplexProcessResult;
import com.damon.workflow.ProcessInstance;
import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.NextState;
import com.damon.workflow.config.State;
import com.damon.workflow.config.StateIdentifier;
import com.damon.workflow.exception.ProcessException;
import com.damon.workflow.process.IProcessCallback;
import com.damon.workflow.process.IProcessRollback;
import com.damon.workflow.process.IProcessor;
import com.damon.workflow.spring.ApplicationContextHelper;
import com.damon.workflow.utils.CollUtils;
import com.damon.workflow.utils.Lists;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 长事务流程引擎
 * <p>
 * 适用于:涉及多个步骤、多个参与者和较长时间跨度的事务
 */
public class LongTransactionProcessEngine extends ProcessEngine {

    private IProcessRollback processRollback;
    private IProcessCallback processCallback;

    protected LongTransactionProcessEngine(Builder builder) {
        super(builder);
        this.processRollback = builder.processRollback;
        this.processCallback = builder.processCallback;
    }

    @Override
    public ComplexProcessResult process(StateIdentifier currentStateIdentifier, Map<String, Object> variables) {
        return super.process(currentStateIdentifier, variables);
    }

    @Override
    public ComplexProcessResult process(StateIdentifier currentStateIdentifier, Map<String, Object> variables, String businessId) {
        return super.process(currentStateIdentifier, variables, businessId);
    }

    @Override
    public ComplexProcessResult process(String processIdentifier, Map<String, Object> variables, String businessId) {
        return super.process(processIdentifier, variables, businessId);
    }

    @Override
    public ComplexProcessResult process(String processIdentifier, Map<String, Object> variables) {
        return super.process(processIdentifier, variables);
    }

    /**
     * 撤回
     *
     * @return
     */
    public StateIdentifier callBack(StateIdentifier currentStateIdentifier, Map<String, Object> variables, String businessId) {
        if (this.processCallback == null) {
            throw new ProcessException("流程撤回功能未配置");
        }
        State state = this.getState(currentStateIdentifier.getCurrentStateProcessIdentifier(), currentStateIdentifier.getCurrentStateId());
        List<String> classProcessors = state.getProcessors();
        ProcessInstance instance = this.getProcessInstance(currentStateIdentifier.getCurrentStateProcessIdentifier());
        RuntimeContext context = new RuntimeContext(instance.getProcessDefinition(), currentStateIdentifier, variables, businessId);
        if (CollUtils.isNotEmpty(classProcessors)) {
            classProcessors.forEach(classProcessor -> {
                IProcessor processor = ApplicationContextHelper.getBean(classProcessor);
                if (processor.isMatch(context)) {
                    processor.preProcess(context);
                }
            });
        }
        StateIdentifier stateIdentifier = this.processCallback.callback(context);
        State nextState = this.getState(stateIdentifier.getCurrentStateProcessIdentifier(), stateIdentifier.getCurrentStateId());
        List<NextState> nextStates = Lists.newList(new NextState(stateIdentifier.getFullPaths(), nextState));
        ComplexProcessResult result = new ComplexProcessResult(false, nextStates);
        result.setCurrentStateIdentifier(currentStateIdentifier);
        if (CollUtils.isNotEmpty(classProcessors)) {
            classProcessors.forEach(classProcessor -> {
                IProcessor processor = ApplicationContextHelper.getBean(classProcessor);
                if (processor.isMatch(context)) {
                    processor.postProcess(result, context);
                }
            });
        }
        return stateIdentifier;
    }

    /**
     * 驳回
     *
     * @param currentStateIdentifier
     * @param businessId
     * @return
     */
    public ComplexProcessResult rollback(StateIdentifier currentStateIdentifier, Map<String, Object> variables, String businessId) {
        if (this.processRollback == null) {
            throw new ProcessException("流程回退功能未配置");
        }
        State state = this.getState(currentStateIdentifier.getCurrentStateProcessIdentifier(), currentStateIdentifier.getCurrentStateId());
        List<String> classProcessors = state.getProcessors();
        ProcessInstance instance = this.getProcessInstance(currentStateIdentifier.getCurrentStateProcessIdentifier());
        RuntimeContext context = new RuntimeContext(instance.getProcessDefinition(), currentStateIdentifier, variables, businessId);
        if (CollUtils.isNotEmpty(classProcessors)) {
            classProcessors.forEach(classProcessor -> {
                IProcessor processor = ApplicationContextHelper.getBean(classProcessor);
                if (processor.isMatch(context)) {
                    processor.preProcess(context);
                }
            });
        }
        List<StateIdentifier> nextStateIdentifiers = this.processRollback.rollback(context);
        List<NextState> nextStates = nextStateIdentifiers.stream().map(nextStateIdentifier -> {
            State nextState = this.getState(nextStateIdentifier.getCurrentStateProcessIdentifier(), nextStateIdentifier.getCurrentStateId());
            return new NextState(nextStateIdentifier.getFullPaths(), nextState);
        }).collect(Collectors.toList());
        ComplexProcessResult result = new ComplexProcessResult(false, nextStates);
        result.setCurrentStateIdentifier(currentStateIdentifier);
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

    public static class Builder extends ProcessEngine.Builder<Builder> {
        private IProcessRollback processRollback;
        private IProcessCallback processCallback;


        public Builder processCallback(IProcessCallback processCallback) {
            this.processCallback = processCallback;
            return this;
        }

        public Builder processRollback(IProcessRollback processRollback) {
            this.processRollback = processRollback;
            return this;
        }

        @Override
        public LongTransactionProcessEngine build() {
            return new LongTransactionProcessEngine(this);
        }

    }

}
