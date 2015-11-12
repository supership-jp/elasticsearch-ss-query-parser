/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.classic;

import java.io.IOException;
import java.io.InputStream;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.search.Query;
import jp.supership.elasticsearch.plugin.queryparser.antlr4.HandleException;
import jp.supership.elasticsearch.plugin.queryparser.antlr4.QueryHandler;
import jp.supership.elasticsearch.plugin.queryparser.dsl.QueryBaseVisitor;
import jp.supership.elasticsearch.plugin.queryparser.dsl.QueryParser;
import jp.supership.elasticsearch.plugin.queryparser.classic.intermediate.QueryEngine;

/**
 * PLACEHOLDER
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class DSQHandler extends QueryBaseVisitor<Query> implements QueryHandler {
    /** Holds query engine which is reponsible for parsing raw query strings. */
    private QueryEngine engine;

    /**
     * {@inheritDoc}
     */
    @Override
    public Query visitQuery(QueryParser.QueryContext context) {
	return this.visitChildren(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query visitClause(QueryParser.ClauseContext context) {
	return this.visitChildren(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query visitExpression(QueryParser.ExpressionContext context) {
	return this.visitChildren(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query visitStringTerm(QueryParser.StringTermContext context) {
	return this.visitChildren(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query visitNumberTerm(QueryParser.NumberTermContext context) {
	return this.visitChildren(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query visitSubQueryTerm(QueryParser.SubQueryTermContext context) {
	return this.visitChildren(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query handle(String defaultField) throws HandleException {
	return this.engine.handle(defaultField);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query dispatch(String field, Token term, Token fuzzySlop, QueryHandler.Context context) throws HandleException {
	return this.engine.dispatch(field, term, fuzzySlop, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fetch(InputStream input) {
	this.engine.fetch(input);
    }
}
