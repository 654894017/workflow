package com.damon.workflow.handler;

import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.StateIdentifier;

import java.util.List;

/**
 * 流程驳回处理器
 */
public interface IProcessRejectionHandler {

    List<StateIdentifier> reject(RuntimeContext context);
}
