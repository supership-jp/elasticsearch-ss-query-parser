/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.handlers;

import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.QueryHandler;
import jp.supership.elasticsearch.plugin.queryparser.handler.ExternalDSQSimpleHandler;

/**
 * This delegate all {@code QueryHandler} functionalities to {@code ExternalDSQSimpleHandler}'s and
 * is responsible for being registered within {@code NamedQueryHandlerFactory}.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class ExternalDSQSimpleHandleDelegator implements QueryHandleDelegator<String> {
    /** Holds handler's name. */
    private static final String NAME = "external_simple";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey() {
	return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryHandler getDelegate(QueryHandlerFactory.Arguments arguments) {
	return new ExternalDSQSimpleHandler(arguments.field, arguments.analyzer);
    }
}
