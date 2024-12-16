package com.damon.workflow.condition_parser;

import com.damon.workflow.ComplexProcessResult;
import com.damon.workflow.config.StateIdentifier;

public interface IProcessRollback {

    ComplexProcessResult rollback(StateIdentifier currentStateIdentifier, String businessId);
}
