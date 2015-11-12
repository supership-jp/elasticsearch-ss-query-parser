/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.antlr4;

import java.io.InputStream;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.search.Query;

/**
 * This interface specifies the implementing class can handle the given {@code java.io.InputStream}
 * as raw query string and instanciates {@code org.apache.lucene.search.Query}.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface QueryHandler {
    /**
     * Represents query handling context, i.e., in accordance to this instance's state, appropriate
     * {@code Query} will be instanciated.
     */
    public class Context {
	public boolean fuzzySlop = false;
	public boolean prefix = false;
	public boolean wildcard = false;
	public boolean fuzzy = false;
	public boolean regexp = false;
    }

    /**
     * Creates {@link org.apache.lucene.search.Query} in accordance with the given raw query string.
     * @param  field the default field for query terms.
     * @throws HandleException if the handling fails.
     */
    public Query handle(String defaultField) throws HandleException;

    /**
     * Dispatches appropriate query-builder in accordance to the given context.
     * @param  field the default field for query terms
     * @param  term the currently handling term token.
     * @param  fuzzySlop the currently handling fuzzy slop term token.
     * @param  context the currently handling context.
     * @throws HandleException if the handling fails.
     */
    public Query dispatch(String field, Token term, Token fuzzySlop, QueryHandler.Context context) throws HandleException;

    /**
     * Fetches the given {@link java.io.InputStream} to this handler.
     * @param  input the default field for query terms.
     * @throws
     */
    public void fetch(InputStream input);
}
