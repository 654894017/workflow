package com.damon.workflow;

import java.util.Set;

public interface IProcessor {
    void process(RuntimeContext context);

    Set<String> stateIds();
}
