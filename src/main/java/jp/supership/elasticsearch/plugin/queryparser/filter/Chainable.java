/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.filter;

import java.io.IOException;
import org.elasticsearch.index.query.QueryParseContext;

/**
 * This interface specifies the implementing class has ability to handle chained process.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface Chainable<R, K, V> {
    /**
     * Returns {@code FieldQuery} in accordance to the assigned configuration.
     * @param  field the currently handling field.
     * @return the resulting {@code Query} instance.
     * @throws IOException if the internal I/O operations have some problem.
     * @throws FilterException if the filtering failed.
     */
    public R doChain(QueryParseContext parseContext) throws IOException, FilterException;

    /**
     * Returns {@code FieldQuery} in accordance to the assigned configuration.
     * @param  field the currently handling field.
     * @param  queryText the currently handling raw query string.
     * @return the resulting {@code Query} instance.
     * @throws ParseException if the parsing fails.
     */
    public R doChain(QueryParseContext parseContext, ChainContext<K, V> chainContext) throws IOException, FilterException;
}
