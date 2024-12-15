package com.damon.workflow.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class NextState {

    private String nextStateFullPaths;

    private State nextState;

    public StateIdentifier getNextStateIdentifier() {
        return StateIdentifier.buildFromFullPaths(nextStateFullPaths);
    }
}
