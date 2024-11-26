// Task.java
package com.damon.workflow.task;


import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.State;

import java.util.Set;

public interface ITask {
    /**
     * 下一个节点
     *
     * @param context
     * @return
     */
    Set<State> execute(RuntimeContext context);

    String getName();

}