/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.classic.intermediate;

import java.io.InputStream;
import org.apache.lucene.analysis.Token;
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
     * Represents query handling context, i.e., in accordance to this instance's state, appropriate
     * {@code Query} will be instanciated.
     */
    protected class Context {
	public boolean fuzzySlop = false;
	public boolean prefix = false;
	public boolean wildcard = false;
	public boolean fuzzy = false;
	public boolean regexp = false;
    }

    /**
     * Creates {@link org.apache.lucene.search.Query} in accordance with the given raw query string.
     * @param  field the default field for query terms.
     * @throws ParseException if the parsing fails.
     */
    public Query handle(String defaultField) throws ParseException;

    /**
     * Dispatches 
     */
    public Query dispatch(String field, Token term, Token fuzzySlop, QueryHandler.Context context) throws ParseException;

    /**
     * Fetches the given {@link java.io.InputStream} to this handler.
     * @param  input the default field for query terms.
     * @throws
     */
    public void fetch(InputStream input);
}
