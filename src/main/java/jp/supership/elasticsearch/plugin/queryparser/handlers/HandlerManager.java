/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.handlers;

import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.QueryHandler;

/**
 * A general-purpose query parser implementation for 
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface HandlerManager<K, V extends QueryHandler> {
    /**
     * Registers {@code QueryHandler}'s classes to be managed.
     * @param key 
     * @param handler the {@code QueryHandler}'s class.
     */
    public void add(K key, V handler);

    /**
     * Dispatches appropriate query-builder in accordance to the given context.
     * @param  context the currently handling context.
     * @throws HandleException if the handling fails.
     */
    public V get(K key);

    /**
     * Dispatches appropriate query-builder in accordance to the given context.
     * @return HandleException if the handling fails.
     */
    public V get();
}
