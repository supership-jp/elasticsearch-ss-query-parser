/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util;

import java.io.Reader;
import org.apache.lucene.search.spans.SpanQuery;

/**
 * This interface specifies the implementing class can handle the given {@code java.io.Reader}
 * as raw query string and instanciates {@code org.apache.lucene.search.spans.SpanQuery}.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface TreeHandler {
    /**
     * Creates {@link org.apache.lucene.search.Query} in accordance with the given raw query string.
     * @param  queryText the raw query string to be parsed.
     * @throws HandleException if the handling fails.
     */
    public SpanQuery handle(String queryText) throws HandleException;

    /**
     * Fetches the given {@link java.io.Reader} to this handler.
     * @param  input the default field for query terms.
     * @throws
     */
    public void fetch(Reader input);
}
