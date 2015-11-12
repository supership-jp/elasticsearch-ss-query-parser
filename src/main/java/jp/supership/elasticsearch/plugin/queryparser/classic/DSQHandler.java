/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.classic;

import java.io.IOException;
import java.io.InputStream;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import jp.supership.elasticsearch.plugin.queryparser.antlr4.HandleException;
import jp.supership.elasticsearch.plugin.queryparser.antlr4.QueryHandler;
import jp.supership.elasticsearch.plugin.queryparser.dsl.QueryBaseVisitor;
import jp.supership.elasticsearch.plugin.queryparser.dsl.QueryParser;
import jp.supership.elasticsearch.plugin.queryparser.classic.intermediate.QueryEngine;
import jp.supership.elasticsearch.plugin.queryparser.util.StringUtils;

/**
 * PLACEHOLDER
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class DSQHandler extends QueryBaseVisitor<Query> implements QueryHandler {
    /**
     * This class is responsible for instanciating Lucene queries from the Supership, inc. Domain Specific
     * Query.
     *
     * @author Shingo OKAWA
     * @since  1.0
     */
    private class Engine extends QueryEngine {
	/** Holds query engine which is reponsible for parsing raw query strings. */
	private QueryHandler handler;

	/** Holds query engine which is reponsible for parsing raw query strings. */
	private InputStream input;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Query handle(String defaultField) throws HandleException {
	    return this.handler.handle(defaultField);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Query dispatch(QueryHandler.Context context) throws HandleException {
	    Query query;

	    try {
		if (context.wildcard) {
		    query = this.getWildcardQuery(context.field, context.term);
		} else if (context.prefix) {
		    query = this.getPrefixQuery(context.field, StringUtils.discardEscapeChar(context.term.substring(0, context.term.length() - 1)));
		} else if (context.regexp) {
		    query = this.getRegexpQuery(context.field, context.term.substring(1, context.term.length() - 1));
		} else if (context.fuzzy) {
		    query = this.dispatchFuzzyQuery(context.field, context.fuzzySlop, context.term);
		} else {
		    query = this.getFieldQuery(context.field, context.term, false);
		}
	    } catch (ParseException cause) {
		throw new HandleException(cause);
	    }

	    return query;
	}

	/**
	 *
	 */
	protected Query dispatchFuzzyQuery(String field, String fuzzySlop, String term) throws ParseException {
	    float fuzzyMinSim = this.getFuzzyMinSim();

	    try {
		// TODO: bit legacy code, so depends on the JRE version, fix this code.
		fuzzyMinSim = Float.valueOf(fuzzySlop.substring(1)).floatValue();
	    } catch (Exception ignorance) {
		// DO NOTHING, the value has its default, so this is safe.
	    }

	    if (fuzzyMinSim < 0.0f) {
		throw new ParseException("minimum similarity for a FuzzyQuery must be between 0.0f and 1.0f.");
	    } else if (fuzzyMinSim >= 1.0f && fuzzyMinSim != (int) fuzzyMinSim) {
		throw new ParseException("fractional edit distances are not allowed.");
	    }

	    return this.getFuzzyQuery(field, term, fuzzyMinSim);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fetch(InputStream input) {
	    this.input = input;
	}
    }

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
	return this.visitQuery(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query dispatch(QueryHandler.Context context) throws HandleException {
	return this.engine.dispatch(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fetch(InputStream input) {
	this.engine.fetch(input);
    }
}
