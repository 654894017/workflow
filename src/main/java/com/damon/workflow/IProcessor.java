package com.damon.workflow;

import java.util.Set;

public interface IProcessor<R> {
    R process(RuntimeContext context);

    Set<String> stateIds();
}
