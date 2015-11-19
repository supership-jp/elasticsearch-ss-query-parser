/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.handlers;

import java.lang.reflect.InvocationTargetException;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.QueryHandler;

/**
 * This interface specifies that the implementing class has ability to be registered
 * within {@code QueryHandlerFactory} registry and all responsible to handle raw query
 * strings to its delegating {@code QueryHandler}.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface QueryHandleDelegator<K> {
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
    public QueryHandler getDelegate(QueryHandlerFactory.Arguments arguments);
}
