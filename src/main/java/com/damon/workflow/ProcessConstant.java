package com.damon.workflow;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProcessConstant {
    public static final String SUB_PROCESS = "SubProcess";

    public static final String START = "Start";

    public static final String USER_TASK = "UserTask";

    public static final String END = "End";

    public static final String EXCLUSIVE_GATEWAY = "ExclusiveGateway";

    public static final String PARALLEL_START_GATEWAY = "ParallelStartGateway";

    public static final String PARALLEL_END_GATEWAY = "ParallelEndGateway";

    public static final String DEFAULT_EVALUATOR = "JavaScript";

    public static boolean isTaskState(String type) {
        return Stream.of(START, USER_TASK, END, SUB_PROCESS).collect(Collectors.toSet()).contains(type);
    }

    public static boolean isSubProcessState(String type) {
        return Stream.of(SUB_PROCESS).collect(Collectors.toSet()).contains(type);
    }

}
