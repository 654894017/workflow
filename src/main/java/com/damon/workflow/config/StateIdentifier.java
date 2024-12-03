package com.damon.workflow.config;

import com.damon.workflow.utils.StrUtils;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class StateIdentifier {

    private List<String> stateIdentifiers;

    public StateIdentifier(String... identifiers) {
        this.stateIdentifiers = Arrays.stream(identifiers).collect(Collectors.toList());
    }

    public StateIdentifier(String identifiers) {
        this.stateIdentifiers = Arrays.asList(identifiers.split(">"));
    }

    public String getFullPaths() {
        return stateIdentifiers.stream().collect(Collectors.joining(">"));
    }

    public String getFullPathsExcludingLast() {
        return stateIdentifiers.subList(0, stateIdentifiers.size() - 1).stream().collect(Collectors.joining(">"));
    }

    public String getParentProcessIdentifier() {
        return stateIdentifiers.get(stateIdentifiers.size() - 4);
    }

    public String getSubProcessStateId() {
        return stateIdentifiers.get(stateIdentifiers.size() - 3);
    }

    public boolean isSubProcess() {
        return stateIdentifiers.size() > 2;
    }

    public String getCurrentStateProcessIdentifier() {
        return stateIdentifiers.get(stateIdentifiers.size() - 2);
    }

    public String getCurrentStateId() {
        return stateIdentifiers.get(stateIdentifiers.size() - 1);
    }

}
