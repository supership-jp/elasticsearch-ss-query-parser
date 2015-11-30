/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.filters;

import jp.supership.elasticsearch.plugin.queryparser.filter.ChainableFilter;

/**
 * This interface specifies the implementing class has functionality to instanciate {@code ChainableFilter}.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface FilterChainFactory<K, T> {
    /**
     * This class is just a placeholder for now. [18/11/2015]
     */
    public static class FilterChainReuseStrategy {
	// PLACEHOLDER
    }

    /**
     * Returns fully-chained {@code ChainableFilter} instance.
     * @return the fully chained {@code ChainableFilter} instance.
     */
    public ChainableFilter<T> create();

    /**
     * Returns associating {@code ChainableFilter} instance.
     * @param  key the key which is associated with the requesting {@code ChainableFilter}.
     * @return the associating {@code QueryHandler} instance.
     */
    public ChainableFilter<T> create(K key) throws IllegalArgumentException;
}
