/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;
import com.google.common.collect.Lists;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.hppc.ObjectFloatOpenHashMap;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.lucene.search.Queries;
import org.elasticsearch.common.regex.Regex;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.util.LocaleUtils;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.query.support.QueryParsers;
import org.elasticsearch.index.query.QueryParser;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.index.query.QueryParsingException;
import org.elasticsearch.index.cache.query.parser.resident.ResidentQueryParserCache;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.ExternalQueryParser;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.HandleException;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.QueryHandler;
import jp.supership.elasticsearch.plugin.queryparser.common.util.StringUtils;
import jp.supership.elasticsearch.plugin.queryparser.filter.ChainableFilter;
import jp.supership.elasticsearch.plugin.queryparser.filters.FilterChainFactory;
import jp.supership.elasticsearch.plugin.queryparser.filters.NamedStringFilterChainFactory;
import jp.supership.elasticsearch.plugin.queryparser.handlers.NamedQueryHandlerFactory;
import jp.supership.elasticsearch.plugin.queryparser.handlers.QueryHandlerFactory;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.config.DSQParserSettings;
import static org.elasticsearch.common.lucene.search.Queries.fixNegativeQueryIfNeeded;

/**
 * A general-purpose query parser implementation for Supership alianses.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class DSQParser implements QueryParser {
    /** Holds plugin's name. */
    public static final String NAME = "hetero_query";

    /** Holds {@code QueryHandlerFactory}. */
    private static final QueryHandlerFactory<String> HANDLER_FACTORY = new NamedQueryHandlerFactory();

    /** Holds {@code QueryHandlerFactory}. */
    private static final FilterChainFactory<String, String> FILTER_FACTORY = new NamedStringFilterChainFactory();

    /** For ES injection-hook. */
    @Inject
    public DSQParser(Settings settings) {
	// DO NOTHING.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] names() {
        return new String[]{NAME, Strings.toCamelCase(NAME)};
    }

    /**
     * Represents metadata for constructing Lucene queries.
     */
    private class Metadata extends DSQParserSettings {
	// Holds currently handling field name.
	private String currentFieldName = null;
	// Holds objective handler's name.
	private String handlerName = null;
	// Holds QueryHandlerFactory's arguments.
	QueryHandlerFactory.Arguments arguments = new QueryHandlerFactory.Arguments();

	// Sets the currently handling field name.
	public void setCurrentFieldName(String currentFieldName) {
	    this.currentFieldName = currentFieldName;
	}

	// Returns the currently handling field name.
	public String getCurrentFieldName() {
	    return this.currentFieldName;
	}

	// Sets the objective handler's name.
	public void setHandlerName(String handlerName) {
	    this.handlerName = handlerName;
	}

	// Setd the objective handler's name.
	public String getHandlerName() {
	    return this.handlerName;
	}

	// Resolves dependency with the assigend context.
	private void prepare(QueryParseContext context) {
	    this.arguments.setDefaultField(this.getDefaultField());
	    this.arguments.setAnalyzer(this.getDefaultAnalyzer());
	    this.arguments.setQueryParseContext(context);
	    this.arguments.setDSQParserConfiguration(this);
	}

	// Setd the objective handler's name.
	public QueryHandlerFactory.Arguments getArgumentsFor(QueryParseContext context) {
	    this.prepare(context);
	    return this.arguments;
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query parse(QueryParseContext context) throws IOException, QueryParsingException {
        XContentParser parser = context.parser();
	Metadata metadata = new Metadata();

        metadata.setDefaultField(context.defaultField());
        metadata.setLenient(context.queryStringLenient());
        metadata.setLocale(Locale.ROOT);
        metadata.setDefaultOperator(ExternalQueryParser.CONJUNCTION_AND);

        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                metadata.setCurrentFieldName(parser.currentName());
            } else if (token == XContentParser.Token.START_ARRAY) {
		this.parseArray(context, parser, metadata, token);
            } else if (token.isValue()) {
		this.parseValue(context, parser, metadata, token);
            }
        }

        if (metadata.getQueryString() == null) {
            throw new QueryParsingException(context.index(), "ss_query_parser must be provided with a [query]");
        }

	metadata.setQueryTypes(context.queryTypes());
        metadata.setDefaultAnalyzer(context.mapperService().searchAnalyzer());
        metadata.setDefaultQuoteAnalyzer(context.mapperService().searchQuoteAnalyzer());
        if (metadata.getEscape()) {
            metadata.setQueryString(StringUtils.escape(metadata.getQueryString()));
        }

        try {
	    Query query = null;
	    this.preprocess(metadata);
	    String queryText = metadata.getQueryString();
	    boolean retrying = false;

            do {
                try {
		    if (retrying == true) {
			queryText = metadata.getQueryString();
                        queryText = queryText.replace("\"", "");
                        queryText = queryText.replace("OR", "or");
			metadata.setQueryString(queryText);
                    }
		    QueryHandler handler = HANDLER_FACTORY.create(metadata.getHandlerName(), metadata.getArgumentsFor(context));
                    query = handler.handle(metadata.getQueryString());
                    if (query == null) {
                        return null;
                    }
                    retrying = false;
                } catch (HandleException exception) {
                    if (retrying == true) {
                        throw exception;
                    }
                    retrying = true;
                }
            } while (retrying);

            if (metadata.getBoost() != DSQParserSettings.DEFAULT_BOOST) {
                query.setBoost(query.getBoost() * metadata.getBoost());
            }
	    if (metadata.getQueryNegation()) {
		query = fixNegativeQueryIfNeeded(query);
	    }
            if (query instanceof BooleanQuery) {
                Queries.applyMinimumShouldMatch((BooleanQuery) query, metadata.getMinimumShouldMatch());
            }

            return query;
        } catch (HandleException cause) {
            throw new QueryParsingException(context.index(), "[ss_query_parser] failed to parse query [" + metadata.getQueryString() + "]", cause);
        }
    }

    /**
     * Executes preprocesses, basically raw query, i.e., string handling.
     * @param  metadata the currently handling metadata.
     * @throws IllegalArgumentException if some configuration file is inconsistent with the pre-settings.
     */
    protected void preprocess(Metadata metadata) throws IllegalArgumentException {
	ChainableFilter<String> chain = FILTER_FACTORY.create();
	metadata.setQueryString(chain.filter(metadata.getQueryString()));
    }

    /**
     * Parses JSON array whithin QueryDSL.
     * @param context the currently handling context.
     * @param parser the responsible parser for JSON.
     * @param metadata the currently rendered metadata of query construction.
     * @param token the currently handling token.
     */
    protected void parseArray(QueryParseContext context, XContentParser parser, Metadata metadata, XContentParser.Token token) throws IOException, QueryParsingException {
	if ("fields".equals(metadata.getCurrentFieldName())) {
	    while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
		String field = null;
		float boost = -1;
		char[] text = parser.textCharacters();
		int end = parser.textOffset() + parser.textLength();
		for (int i = parser.textOffset(); i < end; i++) {
		    if (text[i] == '^') {
			int offset = i - parser.textOffset();
			field = new String(text, parser.textOffset(), offset);
			boost = Float.parseFloat(new String(text, i + 1, parser.textLength() - offset - 1));
			break;
		    }
		}
		if (field == null) {
		    field = parser.text();
		}
		if (metadata.getFields() == null) {
		    metadata.setFields(Lists.<String>newArrayList());
		}
		if (Regex.isSimpleMatchPattern(field)) {
		    for (String index : context.mapperService().simpleMatchToIndexNames(field)) {
			metadata.getFields().add(index);
			if (boost != -1) {
			    if (metadata.getBoosts() == null) {
				metadata.setBoosts(new ObjectFloatOpenHashMap<String>());
			    }
			    metadata.getBoosts().put(index, boost);
			}
		    }
		} else {
		    metadata.getFields().add(field);
		    if (boost != -1) {
			if (metadata.getBoosts() == null) {
			    metadata.setBoosts(new ObjectFloatOpenHashMap<String>());
			}
			metadata.getBoosts().put(field, boost);
		    }
		}
	    }
	} else {
	    throw new QueryParsingException(context.index(), "[ss_query_parser] query does not support [" + metadata.getCurrentFieldName() + "]");
	}
    }

    /**
     * Parses JSON value whithin QueryDSL.
     * @param context the currently handling context.
     * @param parser the responsible parser for JSON.
     * @param metadata the currently rendered metadata of query construction.
     * @param token the currently handling token.
     */
    protected void parseValue(QueryParseContext context, XContentParser parser, Metadata metadata, XContentParser.Token token) throws IOException, QueryParsingException {
	if ("query".equals(metadata.getCurrentFieldName())) {
	    metadata.setQueryString(parser.text());
	} else if ("default_field".equals(metadata.getCurrentFieldName()) || "defaultField".equals(metadata.getCurrentFieldName())) {
	    metadata.setDefaultField(parser.text());
	} else if ("default_operator".equals(metadata.getCurrentFieldName()) || "defaultOperator".equals(metadata.getCurrentFieldName())) {
	    String operator = parser.text();
	    if ("or".equalsIgnoreCase(operator)) {
		metadata.setDefaultOperator(ExternalQueryParser.CONJUNCTION_OR);
	    } else if ("and".equalsIgnoreCase(operator)) {
		metadata.setDefaultOperator(ExternalQueryParser.CONJUNCTION_AND);
	    } else {
		throw new QueryParsingException(context.index(), "[ss_query_parser] query default operator [" + operator + "] is not allowed");
	    }
	} else if ("analyzer".equals(metadata.getCurrentFieldName())) {
	    NamedAnalyzer analyzer = context.analysisService().analyzer(parser.text());
	    if (analyzer == null) {
		throw new QueryParsingException(context.index(), "[ss_query_parser] analyzer [" + parser.text() + "] not found");
	    }
	    metadata.setForcedAnalyzer(analyzer);
	} else if ("quote_analyzer".equals(metadata.getCurrentFieldName()) || "quoteAnalyzer".equals(metadata.getCurrentFieldName())) {
	    NamedAnalyzer analyzer = context.analysisService().analyzer(parser.text());
	    if (analyzer == null) {
		throw new QueryParsingException(context.index(), "[ss_query_parser] quote_analyzer [" + parser.text() + "] not found");
	    }
	    metadata.setForcedQuoteAnalyzer(analyzer);
	} else if ("auto_generate_phrase_queries".equals(metadata.getCurrentFieldName()) || "autoGeneratePhraseQueries".equals(metadata.getCurrentFieldName())) {
	    metadata.setPhraseQueryAutoGeneration(parser.booleanValue());
	} else if ("max_determinized_states".equals(metadata.getCurrentFieldName()) || "maxDeterminizedStates".equals(metadata.getCurrentFieldName())) {
	    metadata.setMaxDeterminizedStates(parser.intValue());
	} else if ("lowercase_expanded_terms".equals(metadata.getCurrentFieldName()) || "lowercaseExpandedTerms".equals(metadata.getCurrentFieldName())) {
	    metadata.setLowercaseExpandedTerms(parser.booleanValue());
	} else if ("enable_position_increments".equals(metadata.getCurrentFieldName()) || "enablePositionIncrements".equals(metadata.getCurrentFieldName())) {
	    metadata.setEnablePositionIncrements(parser.booleanValue());
	} else if ("escape".equals(metadata.getCurrentFieldName())) {
	    metadata.setEscape(parser.booleanValue());
	} else if ("use_dis_max".equals(metadata.getCurrentFieldName()) || "useDisMax".equals(metadata.getCurrentFieldName())) {
	    metadata.setUseDisMax(parser.booleanValue());
	} else if ("in_order".equals(metadata.getCurrentFieldName()) || "inOrder".equals(metadata.getCurrentFieldName())) {
	    metadata.setInOrder(parser.booleanValue());
	} else if ("phrase_slop".equals(metadata.getCurrentFieldName()) || "phraseSlop".equals(metadata.getCurrentFieldName())) {
	    metadata.setPhraseSlop(parser.intValue());
	} else if ("boost".equals(metadata.getCurrentFieldName())) {
	    metadata.setBoost(parser.floatValue());
	} else if ("tie_breaker".equals(metadata.getCurrentFieldName()) || "tieBreaker".equals(metadata.getCurrentFieldName())) {
	    metadata.setTieBreaker(parser.floatValue());
	} else if ("rewrite".equals(metadata.getCurrentFieldName())) {
	    metadata.setRewriteMethod(QueryParsers.parseRewriteMethod(parser.textOrNull()));
	} else if ("minimum_should_match".equals(metadata.getCurrentFieldName()) || "minimumShouldMatch".equals(metadata.getCurrentFieldName())) {
	    metadata.setMinimumShouldMatch(parser.textOrNull());
	} else if ("quote_field_suffix".equals(metadata.getCurrentFieldName()) || "quoteFieldSuffix".equals(metadata.getCurrentFieldName())) {
	    metadata.setQuoteFieldSuffix(parser.textOrNull());
	} else if ("lenient".equalsIgnoreCase(metadata.getCurrentFieldName())) {
	    metadata.setLenient(parser.booleanValue());
	} else if ("locale".equals(metadata.getCurrentFieldName())) {
	    String locale = parser.text();
	    metadata.setLocale(LocaleUtils.parse(locale));
	} else if ("enable_negative_query".equals(metadata.getCurrentFieldName())) {
	    metadata.setQueryNegation(parser.booleanValue());
	} else if ("use_field_refine".equals(metadata.getCurrentFieldName())) {
	    metadata.setFieldRefinement(parser.booleanValue());
	} else if ("handler_name".equals(metadata.getCurrentFieldName())) {
	    metadata.setHandlerName(parser.textOrNull());
	} else {
	    throw new QueryParsingException(context.index(), "[ss_query_parser] query does not support [" + metadata.getCurrentFieldName() + "]");
	}
    }
}
