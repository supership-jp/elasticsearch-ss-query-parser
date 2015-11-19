/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.handlers;

import java.util.Map;
import java.util.HashMap;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.QueryHandler;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.config.QueryEngineConfiguration;

/**
 * This class is responsible for instanciating named {@code QueryHandler}.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class NamedQueryHandlerFactory implements QueryHandlerFactory<String> {
    /** Holds default delegating handler instance. */
    private static final DelegatingQueryHandler<String> DEFAULT_DELEGATING_HANDLER;

    /** Holds map between {@code String} keys and {@code QueryHandler} insatnces. */
    private Map<String, DelegatingQueryhandler<String>> queryHandlers new HashMap<String, DelegatingQueryHandler<String>>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(DelegatingQueryHandler<String> handler) {
	this.queryHandlers.put(handler.getKey(), handler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryHandler create(String key, QueryEngineConfiguration configuration) {
	DelegatingQueryHandler<String> delegatingHandler = this.queryHandlers.get(key);
	if (delegatingHandler == null) {
	    return DEFAULT_DELEGATING_HANDLER.getDelegate(this);
	} else {
	    reuturn delegatingHandler.getDelegate(this);
	}
    }
}
