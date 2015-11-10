/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.classic.intermediate;

import java.io.InputStream;
import org.apache.lucene.search.Query;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 * This interface specifies the implementing class can handle the given {@code java.io.InputStream}
 * as raw query string and instanciates {@code org.apache.lucene.search.Query}.
 *
 * @author Shingo OKAWA
 * @since  09/11/2015
 */
interface QueryHandler {
    /**
     * Creates {@link org.apache.lucene.search.Query} in accordance with the given raw query string.
     * @param  field the default field for query terms.
     * @throws ParseException if the parsing fails.
     */
    public Query handle(String defaultField) throws ParseException;

    /**
     * Fetches the given {@link java.io.InputStream} to this handler.
     * @param  input the default field for query terms.
     * @throws
     */
    public void fetch(InputStream input);
}
