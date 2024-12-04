package com.damon.workflow.gateway;

import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.State;

import java.util.List;

public interface IGateway {

    List<State> execute(RuntimeContext context);

    String getName();

}
