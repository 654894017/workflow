package com.damon.workflow.handler;

import com.damon.workflow.spring.ApplicationContextHelper;

public class ProcessHandlerFactory {

    public static IProcessStateHandler getProcessHandler(String handlerName) {
        return ApplicationContextHelper.getBean(handlerName);
    }

}
