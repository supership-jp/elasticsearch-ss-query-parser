/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.handler;

import java.io.Reader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.HandleException;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.QueryHandler;
import jp.supership.elasticsearch.plugin.queryparser.handlers.QueryHandlerFactory;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.ParseException;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.QueryEngine;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.config.DSQParserConfiguration;
import jp.supership.elasticsearch.plugin.queryparser.util.StringUtils;

/**
 * This class is responsible for handling query parser generated by ANTLR4 and dispatches internal
 * Lucene APIs through the internal QueryEngine implementation.
 * The utilizing parser is generated by {@code jp.supership.elasticsearch.plugin.dsl.Query#g4},
 * hence the corresponding grammer is for "user land" queries.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class ExternalDSQSimpleHandler extends ExternalDSQBaseHandler {
    /** Holds ES logger, this instance's life-cycle is identical to this instance. */
    private ESLogger logger = Loggers.getLogger(ExternalDSQSimpleHandler.class);

    /**
     * This class is responsible for instanciating Lucene queries from the Supership, inc. Domain Specific Query.
     */
    private class Engine extends QueryEngine {
        /** Holds query engine which is reponsible for parsing raw query strings. */
        private QueryHandler handler;

	/**
	 * Constructor.
	 */
	public Engine(QueryHandler handler) {
	    this.handler = handler;
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void configure(DSQParserConfiguration configuration) {
	    // DO NOTHING.
	    // TODO: throws appropriate exception.
	}

        /**
         * {@inheritDoc}
         */
        @Override
        public Query handle(String queryText) throws HandleException {
	    Query query = this.handler.handle(queryText);
	    logger.debug(query.toString());
            return query;
        }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispatch(String field, String term, QueryHandler.Context context) {
	    // wildcard?
	    // prefix?
	    // regexp?
	    // fuzzy?
	}

        /**
         * {@inheritDoc}
         */
        @Override
        public Query dispatchBareToken(QueryHandler.Context context) throws HandleException {
            Query query;

            try {
                if (context.wildcard) {
                    query = this.getWildcardQuery(context.field, context.term);
                } else if (context.prefix) {
                    query = this.getPrefixQuery(context.field, StringUtils.discardEscapeChar(context.term.substring(0, context.term.length() - 1)));
                } else if (context.regexp) {
                    query = this.getRegexpQuery(context.field, context.term.substring(1, context.term.length() - 1));
                } else if (context.fuzzy) {
                    query = this.forwardFuzzyQuery(context.field, context.fuzzySlop, context.term);
                } else {
                    query = this.getFieldQuery(context.field, context.term, false);
                }
            } catch (ParseException cause) {
                throw new HandleException(cause);
            }

            return query;
        }

        /**
         * Forwarding to {@code getFuzzyQuery} method with assigned minimum similarity value.
	 * @param  field the currently handling field.
	 * @param  fuzzySlop the currently handling fuzzly slop term.
	 * @param  term  the currently handling term.
	 * @throws ParseException if the parsing fails.
         */
        private Query forwardFuzzyQuery(String field, String fuzzySlop, String term) throws ParseException {
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
        public Query dispatchQuotedToken(QueryHandler.Context context) throws HandleException {
	    int phraseSlop = this.getPhraseSlop();
	    if (context.fuzzySlop != null) {
		try {
		    phraseSlop = Float.valueOf(context.fuzzySlop.substring(1)).intValue();
		} catch (Exception ignored) {
		    // DO NOTHING, the value has its default, so this is safe.
		}
	    }
	    return this.getFieldQuery(context.field, StringUtils.discardEscapeChar(context.term.substring(1, context.term.length() - 1)), phraseSlop);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void fetch(Reader input) {
            this.handler.fetch(input);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(QueryHandlerFactory.Arguments arguments) {
	this.engine.initialize(arguments.version, arguments.field, arguments.analyzer, arguments.configuration);
    }

    /**
     * Constructor.
     */
    public ExternalDSQSimpleHandler() {
	this.engine = new Engine(this);
	this.state = new ExternalDSQBaseHandler.State();
    }
}
