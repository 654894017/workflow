package com.damon.workflow.handler;


import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.StateIdentifier;

/**
 * 流程撤回处理器
 */
public interface IProcessWithdrawHandler {

    StateIdentifier withdraw(RuntimeContext context);
}
