package com.damon.workflow.utils;

import com.damon.workflow.exception.ProcessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.util.Objects;

/**
 * YAML工具类，用于加载和解析YAML配置文件
 */
public class YamlUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    /**
     * 加载YAML内容并转换为指定类型的对象
     *
     * @param content YAML内容
     * @param clazz   目标类型
     * @param <T>     泛型类型
     * @return 解析后的对象
     * @throws ProcessException 当解析失败时抛出异常
     */
    public static <T> T load(String content, Class<T> clazz) {
        Objects.requireNonNull(content, "Content cannot be null");
        Objects.requireNonNull(clazz, "Class cannot be null");
        
        try {
            return MAPPER.readValue(content, clazz);
        } catch (JsonProcessingException e) {
            throw new ProcessException("加载流程实例配置异常:" + content, e);
        }
    }
}