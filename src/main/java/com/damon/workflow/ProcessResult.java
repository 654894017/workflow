package com.damon.workflow;


import com.damon.workflow.config.State;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProcessResult {
    private boolean completed;
    private String processIdentifier;
    private State currentState;
    private List<State> nextStates;

}
