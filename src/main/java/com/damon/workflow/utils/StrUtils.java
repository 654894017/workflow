package com.damon.workflow.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StrUtils {

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty() || str.trim().isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isEquals(String str1, String str2) {
        return Objects.equals(str1, str2);
    }

    public static boolean isNotEquals(String str1, String str2) {
        return !isEquals(str1, str2);
    }

    public static List<String> split(String processPaths, String regex) {
        return Arrays.stream(processPaths.split(regex)).collect(Collectors.toList());
    }
}
