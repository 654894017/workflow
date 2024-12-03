package com.damon.workflow;


import com.damon.workflow.config.NextState;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ProcessResult3 {
    private boolean completed;
    private List<NextState> nextStates;
}
