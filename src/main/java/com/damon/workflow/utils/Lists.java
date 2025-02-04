package com.damon.workflow.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lists {
    public static <T> List<T> newList(T... elements) {
        List<T> list = new ArrayList<>();
        Arrays.stream(elements).forEach(list::add);
        return list;
    }

}
