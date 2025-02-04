package com.damon.workflow.process;


import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.StateIdentifier;

public interface IProcessCallback {

    StateIdentifier callback(RuntimeContext context);
}
