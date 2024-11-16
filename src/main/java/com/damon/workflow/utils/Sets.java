package com.damon.workflow.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Sets {
    public static <T> Set<T> newHashSet(T... elements) {
        HashSet<T> sets = new HashSet<>();
        Arrays.stream(elements).forEach(sets::add);
        return sets;
    }

}
