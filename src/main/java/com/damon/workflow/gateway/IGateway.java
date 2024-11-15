package com.damon.workflow.gateway;

import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.State;

import java.util.Set;

public interface IGateway {

    Set<State> execute(RuntimeContext context);

    String getName();

}
