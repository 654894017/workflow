// Task.java
package com.damon.workflow.task;


import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.State;

import java.util.List;

public interface ITask {
    /**
     * 下一个节点
     *
     * @param context
     * @return
     */
    List<State> execute(RuntimeContext context);

    String getName();

}