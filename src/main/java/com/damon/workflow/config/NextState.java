package com.damon.workflow.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class NextState {

    private String currentStateFullPaths;

    private State currentState;

    private String nextStateFullPaths;

    private State nextState;


}
