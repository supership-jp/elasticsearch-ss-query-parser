/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.classic;

import java.io.InputStream;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import jp.supership.elasticsearch.plugin.queryparser.antlr4.HandleException;
import jp.supership.elasticsearch.plugin.queryparser.antlr4.QueryHandler;
import jp.supership.elasticsearch.plugin.queryparser.classic.intermediate.QueryEngine;
import jp.supership.elasticsearch.plugin.queryparser.util.StringUtils;

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
    public Query dispatch(QueryHandler.Context context) throws HandleException {
	Query query;
	String term = context.term == null ? "" : context.term;
	int length = term.length();

	try {
	    if (context.wildcard) {
		query = this.getWildcardQuery(context.field, term);
	    } else if (context.prefix) {
		query = this.getPrefixQuery(context.field, StringUtils.discardEscapeChar(term.substring(0, length - 1)));
	    } else if (context.regexp) {
		query = this.getRegexpQuery(context.field, term.substring(1, length - 1));
	    } else if (context.fuzzy) {
		query = this.dispatchFuzzyQuery(context.field, context.fuzzySlop, term);
	    } else {
		query = this.getFieldQuery(context.field, term, false);
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
	// PLACEHOLDER
    }
}
