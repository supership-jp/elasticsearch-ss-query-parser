/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.handlers;

import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.Analyzer;
import org.elasticsearch.index.query.QueryParseContext;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.QueryHandler;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.config.DSQParserConfiguration;

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
     * This class specifies {@code QueryHandler} instanciation arguments.
     */
    public static class Arguments {
	/** Holds responsible Luecne's version. */
	private Version luceneVersion;
	/** Holds assigned default field. */
	private String defaultField;
	/** Holds assigned analyzer. */
	private Analyzer analyzer;
	/** Holds currently handling context. */
	private QueryParseContext context;
	/** Holds assigned configuration. */
	private DSQParserConfiguration configuration;

	/** Returns the assigned Lucene version. */
	public Version getLuceneVersion() {
	    return this.luceneVersion;
	}

	/** Sets the Lucene version. */
	public void setLuceneVersion(Version luceneVersion) {
	    this.luceneVersion = luceneVersion;
	}

	/** Returns the assigned default field. */
	public String getDefaultField() {
	    return this.defaultField;
	}

	/** Sets the default field. */
	public void setDefaultField(String defaultField) {
	    this.defaultField = defaultField;
	}

	/** Returns the assigned analyzer. */
	public Analyzer getAnalyzer() {
	    return this.analyzer;
	}

	/** Sets the analyzer. */
	public void setAnalyzer(Analyzer analyzer) {
	    this.analyzer = analyzer;
	}

	/** Returns the assigned query parsing context. */
	public QueryParseContext getQueryParseContext() {
	    return this.context;
	}

	/** Sets the query parsing context. */
	public void setQueryParseContext(QueryParseContext context) {
	    this.context = context;
	}

	/** Returns the assigned DSQ parser configuration. */
	public DSQParserConfiguration getDSQParserConfiguration() {
	    return this.configuration;
	}

	/** Sets the query DSQ parser configuration. */
	public void setDSQParserConfiguration(DSQParserConfiguration configuration) {
	    this.configuration = configuration;
	}
    }

    /**
     * Returns wrapped {@code QueryHandler} instance.
     * @param  key the key which is associated with the requesting {@code QueryHandler}.
     * @param  configuration the configuration which will be used for instanciating {@code QueryHandler}.
     * @return the wrapped {@code QueryHandler} instance.
     */
    public QueryHandler create(K key, Arguments arguments) throws IllegalArgumentException;
}
