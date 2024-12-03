package com.damon.workflow.config;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class NextState {

    private String currentStateIdentifiers;

    private State currentState;

    private String nextStateStateIdentifiers;

    private State nextState;


}
