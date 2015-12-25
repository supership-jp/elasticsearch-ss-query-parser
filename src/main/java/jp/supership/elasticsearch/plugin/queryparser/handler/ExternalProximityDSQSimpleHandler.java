/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.handler;

import java.io.Reader;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.HandleException;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.TreeHandler;
import jp.supership.elasticsearch.plugin.queryparser.handlers.QueryHandlerFactory;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.ParseException;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.ProximityQueryEngine;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.config.DSQParserConfiguration;

/**
 * This class is responsible for handling query parser generated by ANTLR4 and dispatches internal
 * Lucene APIs through the internal QueryEngine implementation.
 * The utilizing parser is generated by {@code jp.supership.elasticsearch.plugin.dsl.Query#g4},
 * hence the corresponding grammer is for "user land" queries.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class ExternalProximityDSQSimpleHandler extends ExternalProximityDSQBaseHandler {
    /**
     * This class is responsible for instanciating Lucene queries from the Supership, inc. Domain Specific Query.
     */
    private class Engine extends ProximityQueryEngine {
        /** Holds query engine which is reponsible for parsing raw query strings. */
        private TreeHandler handler;

	/**
	 * Constructor.
	 */
	public Engine(TreeHandler handler) {
	    this.handler = handler;
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void configure(DSQParserConfiguration configuration) {
	    // DO NOTHING.
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(QueryHandlerFactory.Arguments arguments) {
	this.engine.setContext(arguments.getQueryParseContext());
	if (arguments.getLuceneVersion() != null) {
	    this.engine.initialize(arguments.getLuceneVersion(), arguments.getDefaultField(), arguments.getAnalyzer(), arguments.getDSQParserConfiguration());
	} else {
	    this.engine.initialize(arguments.getDefaultField(), arguments.getAnalyzer(), arguments.getDSQParserConfiguration());
	}
	this.engine.configure(arguments.getDSQParserConfiguration());
    }

    /**
     * Constructor.
     */
    public ExternalProximityDSQSimpleHandler() {
	this.engine = new Engine(this);
	this.metadata = new ExternalProximityDSQBaseHandler.Metadata();
    }
}
