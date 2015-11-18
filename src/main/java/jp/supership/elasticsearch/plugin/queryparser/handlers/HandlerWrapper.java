/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.handlers;

import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.QueryHandler;

/**
 * This class is 
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface QueryHandlerWrapper extends QueryHandler {
    /**
     * This class is just a placeholder for now. [18/11/2015]
     */
    public static class QueryHandlerReuseStrategy {
	// PLACEHOLDER
    }

    /**
     * Dispatches appropriate query-builder in accordance to the given context.
     * @param  context the currently handling context.
     * @throws HandleException if the handling fails.
     */
    public QueryHandler getWrappedHandler();
}
