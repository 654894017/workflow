package com.damon.workflow;


import com.damon.workflow.config.NextState;
import com.damon.workflow.config.StateIdentifier;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ComplexProcessResult {
    private boolean completed;
    private StateIdentifier currentStateIndentifier;
    private List<NextState> nextStates;

    public ComplexProcessResult(boolean completed, List<NextState> nextStates) {
        this.completed = completed;
        this.nextStates = nextStates;
    }
}
