package com.damon.workflow.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 忽略key大小写的Map
 *
 * @param <V>
 */
public class CaseInsensitiveMap<V> {
    private final Map<String, V> map = new HashMap<>();

    public void put(String key, V value) {
        map.put(key.toLowerCase(), value);
    }

    public V get(String key) {
        return map.get(key.toLowerCase());
    }

    public Map<String, V> getMap() {
        return map;
    }

    public V getOrDefault(String key, V value) {
        return map.getOrDefault(key.toLowerCase(), value);
    }
}