/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.classic;

import java.io.InputStream;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.search.Query;
import jp.supership.elasticsearch.plugin.queryparser.antlr4.HandleException;
import jp.supership.elasticsearch.plugin.queryparser.antlr4.QueryHandler;
import jp.supership.elasticsearch.plugin.queryparser.classic.intermediate.QueryEngine;

/**
 * This class is responsible for instanciating Lucene queries, query parser delegates all sub-query
 * instanciation tasks to this class. This implementation assumes that concrete query handler will be 
 * constructed in accordance to the given ANTLR grammar.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class DSQEngine extends QueryEngine {
    /**
     * {@inheritDoc}
     */
    @Override
    public Query handle(String defaultField) throws HandleException {
	// PLACEHOLDER
	return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query dispatch(String field, Token term, Token fuzzySlop, QueryHandler.Context context) throws HandleException {
	// PLACEHOLDER
	return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fetch(InputStream input) {
	// PLACEHOLDER
    }
}
