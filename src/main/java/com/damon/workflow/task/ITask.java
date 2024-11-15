// Task.java
package com.damon.workflow.task;


import com.damon.workflow.RuntimeContext;
import com.damon.workflow.config.State;

public interface ITask {
    /**
     * 下一个节点
     *
     * @param context
     * @return
     */
    State execute(RuntimeContext context);

    String getName();

}


