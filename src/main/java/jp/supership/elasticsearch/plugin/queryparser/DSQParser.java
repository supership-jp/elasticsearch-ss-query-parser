/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Locale;
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
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.HandleException;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.QueryHandler;
import jp.supership.elasticsearch.plugin.queryparser.handlers.ExternalDSQMapperHandleDelegator;
import jp.supership.elasticsearch.plugin.queryparser.handlers.ExternalDSQSimpleHandleDelegator;
import jp.supership.elasticsearch.plugin.queryparser.handlers.NamedQueryHandlerFactory;
import jp.supership.elasticsearch.plugin.queryparser.handlers.QueryHandlerFactory;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.config.QueryEngineDSLSettings;
import jp.supership.elasticsearch.plugin.queryparser.util.StringUtils;
import static org.elasticsearch.common.lucene.search.Queries.fixNegativeQueryIfNeeded;

/**
 * A general-purpose query parser implementation for Supership alianses.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class DSQParser implements QueryParser {
    /** Holds plugin's name. */
    public static final String NAME = "ss_query_parser";

    /** Holds {@code QueryHandlerFactory}. */
    private static final QueryHandlerFactory<String> HANDLER_FACTORY = new NamedQueryHandlerFactory();

    static {
	HANDLER_FACTORY.register(new ExternalDSQMapperHandleDelegator());
	HANDLER_FACTORY.register(new ExternalDSQSimpleHandleDelegator());
    }

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
     * {@inheritDoc}
     */
    @Override
    public Query parse(QueryParseContext context) throws IOException, QueryParsingException {
        XContentParser parser = context.parser();
	String currentFieldName = null;
	String queryKind = null;
        QueryEngineDSLSettings settings = new QueryEngineDSLSettings();
	QueryHandlerFactory.Arguments arguments = new QueryHandlerFactory.Arguments();
        settings.setDefaultField(context.defaultField());
        settings.setLenient(context.queryStringLenient());
        settings.setLocale(Locale.ROOT);
        settings.setDefaultOperator(jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.QueryParser.CONJUNCTION_AND);

        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token == XContentParser.Token.START_ARRAY) {
                if ("fields".equals(currentFieldName)) {
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
                        if (settings.getFields() == null) {
                            settings.setFields(Lists.<String>newArrayList());
                        }
                        if (Regex.isSimpleMatchPattern(field)) {
                            for (String index : context.mapperService().simpleMatchToIndexNames(field)) {
                                settings.getFields().add(index);
                                if (boost != -1) {
                                    if (settings.getBoosts() == null) {
                                        settings.setBoosts(new ObjectFloatOpenHashMap<String>());
                                    }
                                    settings.getBoosts().put(index, boost);
                                }
                            }
                        } else {
                            settings.getFields().add(field);
                            if (boost != -1) {
                                if (settings.getBoosts() == null) {
                                    settings.setBoosts(new ObjectFloatOpenHashMap<String>());
                                }
                                settings.getBoosts().put(field, boost);
                            }
                        }
                    }
                } else {
                    throw new QueryParsingException(context.index(), "[ss_query_parser] query does not support [" + currentFieldName + "]");
                }
            } else if (token.isValue()) {
                if ("query".equals(currentFieldName)) {
                    settings.setQueryString(parser.text());
                } else if ("default_field".equals(currentFieldName) || "defaultField".equals(currentFieldName)) {
                    settings.setDefaultField(parser.text());
                } else if ("default_operator".equals(currentFieldName) || "defaultOperator".equals(currentFieldName)) {
                    String operator = parser.text();
                    if ("or".equalsIgnoreCase(operator)) {
                        settings.setDefaultOperator(jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.QueryParser.CONJUNCTION_OR);
                    } else if ("and".equalsIgnoreCase(operator)) {
                        settings.setDefaultOperator(jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.QueryParser.CONJUNCTION_AND);
                    } else {
                        throw new QueryParsingException(context.index(), "[ss_query_parser] query default operator [" + operator + "] is not allowed");
                    }
                } else if ("analyzer".equals(currentFieldName)) {
                    NamedAnalyzer analyzer = context.analysisService().analyzer(parser.text());
                    if (analyzer == null) {
                        throw new QueryParsingException(context.index(), "[ss_query_parser] analyzer [" + parser.text() + "] not found");
                    }
                    settings.setForcedAnalyzer(analyzer);
                } else if ("quote_analyzer".equals(currentFieldName) || "quoteAnalyzer".equals(currentFieldName)) {
                    NamedAnalyzer analyzer = context.analysisService().analyzer(parser.text());
                    if (analyzer == null) {
                        throw new QueryParsingException(context.index(), "[ss_query_parser] quote_analyzer [" + parser.text() + "] not found");
                    }
                    settings.setForcedQuoteAnalyzer(analyzer);
                } else if ("auto_generate_phrase_queries".equals(currentFieldName) || "autoGeneratePhraseQueries".equals(currentFieldName)) {
                    settings.setPhraseQueryAutoGeneration(parser.booleanValue());
                } else if ("max_determinized_states".equals(currentFieldName) || "maxDeterminizedStates".equals(currentFieldName)) {
                    settings.setMaxDeterminizedStates(parser.intValue());
                } else if ("lowercase_expanded_terms".equals(currentFieldName) || "lowercaseExpandedTerms".equals(currentFieldName)) {
                    settings.setLowercaseExpandedTerms(parser.booleanValue());
                } else if ("enable_position_increments".equals(currentFieldName) || "enablePositionIncrements".equals(currentFieldName)) {
                    settings.setEnablePositionIncrements(parser.booleanValue());
                } else if ("escape".equals(currentFieldName)) {
                    settings.setEscape(parser.booleanValue());
                } else if ("use_dis_max".equals(currentFieldName) || "useDisMax".equals(currentFieldName)) {
                    settings.setUseDisMax(parser.booleanValue());
                } else if ("phrase_slop".equals(currentFieldName) || "phraseSlop".equals(currentFieldName)) {
                    settings.setPhraseSlop(parser.intValue());
                } else if ("boost".equals(currentFieldName)) {
                    settings.setBoost(parser.floatValue());
                } else if ("tie_breaker".equals(currentFieldName) || "tieBreaker".equals(currentFieldName)) {
                    settings.setTieBreaker(parser.floatValue());
                } else if ("rewrite".equals(currentFieldName)) {
                    settings.setRewriteMethod(QueryParsers.parseRewriteMethod(parser.textOrNull()));
                } else if ("minimum_should_match".equals(currentFieldName) || "minimumShouldMatch".equals(currentFieldName)) {
                    settings.setMinimumShouldMatch(parser.textOrNull());
                } else if ("quote_field_suffix".equals(currentFieldName) || "quoteFieldSuffix".equals(currentFieldName)) {
                    settings.setQuoteFieldSuffix(parser.textOrNull());
                } else if ("lenient".equalsIgnoreCase(currentFieldName)) {
                    settings.setLenient(parser.booleanValue());
                } else if ("locale".equals(currentFieldName)) {
                    String locale = parser.text();
                    settings.setLocale(LocaleUtils.parse(locale));
                } else if ("enable_negative_query".equals(currentFieldName)) {
		    settings.setQueryNegation(parser.booleanValue());
		} else if ("use_field_refine".equals(currentFieldName)) {
                    settings.setFieldRefinement(parser.booleanValue());
		} else if ("query_kind".equals(currentFieldName)) {
                    queryKind = parser.textOrNull();
                } else {
                    throw new QueryParsingException(context.index(), "[ss_query_parser] query does not support [" + currentFieldName + "]");
                }
            }
        }

        if (settings.getQueryString() == null) {
            throw new QueryParsingException(context.index(), "so_query must be provided with a [query]");
        }

	settings.setQueryTypes(context.queryTypes());
        settings.setDefaultAnalyzer(context.mapperService().searchAnalyzer());
        settings.setDefaultQuoteAnalyzer(context.mapperService().searchQuoteAnalyzer());
        if (settings.getEscape()) {
            settings.setQueryString(StringUtils.escape(settings.getQueryString()));
        }

	arguments.field = settings.getDefaultField();
	arguments.analyzer = settings.getDefaultAnalyzer();
	arguments.context = context;
	arguments.configuration = settings;

        try {
	    Query query = null;
	    String queryText = settings.getQueryString();
	    queryText = Normalizer.normalize(queryText, Normalizer.Form.NFKC);
	    queryText = queryText.replaceAll("\\s+", " ");
	    settings.setQueryString(queryText);
	    boolean retrying = false;
            do {
                try {
		    if (retrying == true) {
			queryText = settings.getQueryString();
                        queryText = queryText.replace("\"", "");
                        queryText = queryText.replace("OR", "or");
			settings.setQueryString(queryText);
                    }
		    // TODO: FIX THIS
		    QueryHandler handler = HANDLER_FACTORY.create(queryKind, arguments);
                    query = handler.handle(settings.getQueryString());
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

            if (settings.getBoost() != QueryEngineDSLSettings.DEFAULT_BOOST) {
                query.setBoost(query.getBoost() * settings.getBoost());
            }
	    if (settings.getQueryNegation()) {
		query = fixNegativeQueryIfNeeded(query);
	    }
            if (query instanceof BooleanQuery) {
                Queries.applyMinimumShouldMatch((BooleanQuery) query, settings.getMinimumShouldMatch());
            }
            return query;
        } catch (HandleException cause) {
            throw new QueryParsingException(context.index(), "[ss_query_parser] failed to parse query [" + settings.getQueryString() + "]", cause);
        }
    }
}
