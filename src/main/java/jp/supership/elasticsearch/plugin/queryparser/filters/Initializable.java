/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.filters;

import java.util.Map;

/**
 * This interface specifies that the implementing class has ability to be registered
 * within {@code FilterChainFactory} registry.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface Initializable {
    /**
     * Hooks some initialization protocols.
     */
    public void initialize(Map<String, Object> settings) throws IllegalArgumentException;
}
