package com.damon.workflow.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class StateIdentifier {

    private static final String SEPARATOR = ">";

    private List<String> stateIdentifiers;

    private StateIdentifier(String... identifiers) {
        this.stateIdentifiers = Arrays.stream(identifiers).collect(Collectors.toList());
    }

    private StateIdentifier(String stateFullPaths) {
        this.stateIdentifiers = Arrays.asList(stateFullPaths.split(SEPARATOR));
    }

    public static StateIdentifier buildFromIdentifiers(String... stateIdentifiers) {
        return new StateIdentifier(stateIdentifiers);
    }

    public static StateIdentifier buildFromFullPaths(String stateFullPaths, String... stateIdentifiers) {
        String statePath = Arrays.stream(stateIdentifiers).collect(Collectors.joining(SEPARATOR));
        return new StateIdentifier(stateFullPaths + SEPARATOR + statePath);
    }

    /**
     * 主流程id > 子流程节点id > 子流程id > 子流程节点id,
     * <p>
     * 例如: performanceReview:1.0 > SubProcess1 > SubProcess2:1.0 > Start
     *
     * @param stateFullPaths
     */
    public static StateIdentifier buildFromFullPaths(String stateFullPaths) {
        return new StateIdentifier(stateFullPaths);
    }

    public String getFullPaths() {
        return stateIdentifiers.stream().collect(Collectors.joining(SEPARATOR));
    }

    public String getFullPathsExcludingLast() {
        return stateIdentifiers.subList(0, stateIdentifiers.size() - 1).stream().collect(Collectors.joining(SEPARATOR));
    }


    public String getParentProcessFullPaths() {
        return stateIdentifiers.subList(0, stateIdentifiers.size() - 3).stream().collect(Collectors.joining(SEPARATOR));
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
