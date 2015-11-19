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
    private final QueryHandleDelegator<String> defaultDelegator = new ExternalDSQMapperHandleDelegator();

    /** Holds map between {@code String} keys and {@code QueryHandler} insatnces. */
    private final Map<String, QueryHandleDelegator<String>> delegators = new HashMap<String, QueryHandleDelegator<String>>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(QueryHandleDelegator<String> delegator) {
	this.delegators.put(delegator.getKey(), delegator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryHandler create(String key, QueryHandlerFactory.Arguments arguments) {
	QueryHandleDelegator<String> delegator = this.delegators.get(key);
	if (delegator == null) {
	    delegator = this.defaultDelegator;
	}
	reuturn delegator.getDelegate(arguments);
    }
}
