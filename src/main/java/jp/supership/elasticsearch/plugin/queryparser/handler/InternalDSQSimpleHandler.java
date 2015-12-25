/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.handler;

import java.io.Reader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.HandleException;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.DispatcherHandler;
import jp.supership.elasticsearch.plugin.queryparser.common.util.StringUtils;
import jp.supership.elasticsearch.plugin.queryparser.handlers.QueryHandlerFactory;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.ParseException;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.QueryEngine;
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
public class InternalDSQSimpleHandler extends InternalDSQBaseHandler {
    /**
     * This class is responsible for instanciating Lucene queries from the Supership, inc. Domain Specific Query.
     */
    private class Engine extends QueryEngine {
        /** Holds query engine which is reponsible for parsing raw query strings. */
        private DispatcherHandler handler;

	/**
	 * Constructor.
	 */
	public Engine(DispatcherHandler handler) {
	    this.handler = handler;
	}

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void configure(DSQParserConfiguration configuration) {
	    // DO NOTHING.
	}

        /**
         * {@inheritDoc}
         */
        @Override
        public Query handle(String queryText) throws HandleException {
	    return this.handler.handle(queryText);
        }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispatch(DispatcherHandler.Context context) {
	    // THIS IS PLACEHOLDER.
	}

        /**
         * {@inheritDoc}
         */
        @Override
        public Query dispatchBareToken(DispatcherHandler.Context context) throws HandleException {
            Query query;

            try {
                if (context.preferWildcard()) {
                    query = this.getWildcardQuery(context.getField(), context.getTerm());
                } else if (context.preferPrefix()) {
                    query = this.getPrefixQuery(context.getField(), StringUtils.discardEscapeChar(context.getTerm().substring(0, context.getTerm().length() - 1)));
                } else if (context.preferRegexp()) {
                    query = this.getRegexpQuery(context.getField(), context.getTerm().substring(1, context.getTerm().length() - 1));
                } else if (context.preferFuzzy()) {
                    query = this.forwardFuzzyQuery(context.getField(), context.getFuzzySlop(), context.getTerm());
                } else {
                    query = this.getFieldQuery(context.getField(), context.getTerm(), false);
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
        public Query dispatchQuotedToken(DispatcherHandler.Context context) throws HandleException {
	    int phraseSlop = this.getPhraseSlop();
	    if (context.getFuzzySlop() != null) {
		try {
		    phraseSlop = Float.valueOf(context.getFuzzySlop().substring(1)).intValue();
		} catch (Exception ignored) {
		    // DO NOTHING, the value has its default, so this is safe.
		}
	    }
	    return this.getFieldQuery(context.getField(), StringUtils.discardEscapeChar(context.getTerm().substring(1, context.getTerm().length() - 1)), phraseSlop);
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
    public InternalDSQSimpleHandler() {
	this.engine = new Engine(this);
	this.metadata = new InternalDSQBaseHandler.Metadata();
    }
}
