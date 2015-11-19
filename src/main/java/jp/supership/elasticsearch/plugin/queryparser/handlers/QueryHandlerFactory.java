/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.handlers;

import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.QueryHandler;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.config.QueryEngineConfiguration;

/**
 * This interface specifies the implementing class has functionality to instanciate {@code QueryHandler}.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface QueryHandlerFactory<K> {
    /**
     * This class is just a placeholder for now. [18/11/2015]
     */
    public static class QueryHandlerReuseStrategy {
	// PLACEHOLDER
    }

    /**
     * Registers {@code QueryHandler} which is wrapped within {@code DelegatingQueryHandler}.
     * @param handler the registering delegating query handler.
     */
    public void register(DelegatingQueryHandler<K> handler);

    /**
     * Returns wrapped {@code QueryHandler} instance.
     * @param  key the key which is associated with the requesting {@code QueryHandler}.
     * @param  configuration the configuration which will be used for instanciating {@code QueryHandler}.
     * @return the wrapped {@code QueryHandler} instance.
     */
    public QueryHandler create(K key, QueryEngineConfiguration configuration);
}
