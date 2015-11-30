/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.filter;

/**
 * This interface specifies the implementing class has ability to handle filtering process.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface Filter<T> {
    /**
     * Filters the given target, this method could be destructive.
     * @param  target the instance to be filtered.
     * @param  context the currently handling filtering context.
     * @throws FilterException if the filtering fails.
     */
    public T doFilter(T target, FilterContext<String, Object> context) throws FilterException;
}
