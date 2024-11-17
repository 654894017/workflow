// Task.java
package com.damon.workflow.task;


import com.damon.workflow.RuntimeContext;

public interface ITask {
    /**
     * 下一个节点
     *
     * @param context
     * @return
     */
    void execute(RuntimeContext context);

    String getName();

}


