package com.damon.workflow.utils;

import com.damon.workflow.exception.ProcessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class YamlUtils {

    private static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public static <T> T load(String content, Class<T> clazz) {
        try {
            return mapper.readValue(content, clazz);
        } catch (JsonProcessingException e) {
            throw new ProcessException("加载流程实例配置异常", e);
        }
    }


}
