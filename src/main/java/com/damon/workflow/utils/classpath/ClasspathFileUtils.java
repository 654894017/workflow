package com.damon.workflow.utils.classpath;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class ClasspathFileUtils {

    /**
     * 从classpath中读取文件内容为字符串
     *
     * @param filePath classpath中的文件路径
     * @return 文件内容
     * @throws RuntimeException 如果文件读取失败
     */
    public static String readFileAsString(String filePath) {
        try (InputStream inputStream = getResourceAsStream(filePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file from classpath: " + filePath, e);
        }
    }

    /**
     * 获取classpath中的文件输入流
     *
     * @param filePath classpath中的文件路径
     * @return 文件的输入流
     * @throws RuntimeException 如果文件未找到
     */
    private static InputStream getResourceAsStream(String filePath) {
        InputStream inputStream = ClasspathFileUtils.class.getClassLoader().getResourceAsStream(filePath);
        if (inputStream == null) {
            throw new RuntimeException("File not found in classpath: " + filePath);
        }
        return inputStream;
    }
}