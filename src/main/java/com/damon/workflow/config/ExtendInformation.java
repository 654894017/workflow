package com.damon.workflow.config;

import java.util.List;

public class ExtendInformation {
    private List<String> allowEditingResouces;
    private List<String> actions;

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public List<String> getAllowEditingResouces() {
        return allowEditingResouces;
    }

    public void setAllowEditingResouces(List<String> allowEditingResouces) {
        this.allowEditingResouces = allowEditingResouces;
    }
}
