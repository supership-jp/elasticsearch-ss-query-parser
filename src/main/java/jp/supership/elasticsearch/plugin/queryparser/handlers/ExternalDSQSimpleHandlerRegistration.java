/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.handlers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.QueryHandler;
import jp.supership.elasticsearch.plugin.queryparser.handler.ExternalDSQSimpleHandler;

/**
 * This interface specifies that the implementing class has ability to handle {@code QueryHandlerFactory}'s
 * registration functionality.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class ExternalDSQSimpleHandler implements DelegatingQueryHandler<String> {
    /** Holds handler's name. */
    private static final String NAME = "external_simple";

    /** */
    private static Constructor<ExternalDSQSimpleHandler> CONSTRUCTOR;
    
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
    public QueryHandler getDelegate(QueryHandlerFactory factory)
	throws InstanciationException,
	       IllegalAccessException,
	       IllegalArgumentException,
	       InvocationTargetException {
    }
}
