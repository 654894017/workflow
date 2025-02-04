package com.damon.workflow.process;

import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.StateIdentifier;

import java.util.List;

public interface IProcessRollback {

    List<StateIdentifier> rollback(RuntimeContext context);
}
