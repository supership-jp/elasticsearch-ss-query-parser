/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.handlers;

import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.QueryHandler;

/**
 * This interface specifies that the implementing class has ability to handle {@code QueryHandlerFactory}'s
 * registration functionality.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface DelegatingQueryHandler<K> {
    /**
     * This class is just a placeholder for now. [18/11/2015]
     */
    public static class ReuseStrategy {
	// PLACEHOLDER
    }

    /**
     * Returns wrapped {@code QueryHandler}'s key which will be used {@code QueryHandlerFactory}.
     * @return the wrapped {@code QueryHandler}'s key.
     */
    public K getKey();

    /**
     * Returns wrapped {@code QueryHandler} instance.
     * @return the wrapped {@code QueryHandler} instance.
     */
    public QueryHandler getDelegate(QueryHandlerFactory factory);
}
