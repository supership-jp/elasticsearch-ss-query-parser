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
	// Holds responsible Luecne's version.
	public Version version;
	// Holds assigned default field.
	public String field;
	// Holds assigned analyzer.
	public Analyzer analyzer;
	// Holds currently handling context.
	public QueryParseContext context;
	// Holds assigned configuration.
	public DSQParserConfiguration configuration;
    }

    /**
     * Returns wrapped {@code QueryHandler} instance.
     * @param  key the key which is associated with the requesting {@code QueryHandler}.
     * @param  configuration the configuration which will be used for instanciating {@code QueryHandler}.
     * @return the wrapped {@code QueryHandler} instance.
     */
    public QueryHandler create(K key, Arguments arguments) throws IllegalArgumentException;
}
