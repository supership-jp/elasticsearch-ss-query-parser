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
import jp.supership.elasticsearch.plugin.queryparser.handler.ExternalDSLMapperHandler;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.config.QueryEngineDSLSettings;
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

    /** Holds plugin's name. */
    private HandlerManager handlerManager;

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
        String queryName = null;
	String currentFieldName = null;
        QueryEngineDSLSettings settings = new QueryEngineDSLSettings();
        settings.setDefaultField(context.defaultField());
        settings.setLenient(context.queryStringLenient());
        settings.setLocale(Locale.ROOT);
        settings.setDefaultOperator(jp.scaleout.elasticsearch.plugins.queryparser.classic.QueryParser.Operator.AND);

        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token == XContentParser.Token.START_ARRAY) {
                if ("fields".equals(currentFieldName)) {
                    while ((token = parser.nextToken()) != XContentParser.Token.END_ARRAY) {
                        String fField = null;
                        float fBoost = -1;
                        char[] text = parser.textCharacters();
                        int end = parser.textOffset() + parser.textLength();
                        for (int i = parser.textOffset(); i < end; i++) {
                            if (text[i] == '^') {
                                int relativeLocation = i - parser.textOffset();
                                fField = new String(text, parser.textOffset(), relativeLocation);
                                fBoost = Float.parseFloat(new String(text, i + 1, parser.textLength() - relativeLocation - 1));
                                break;
                            }
                        }
                        if (fField == null) {
                            fField = parser.text();
                        }
                        if (settings.fields() == null) {
                            settings.fields(Lists.<String>newArrayList());
                        }

                        if (Regex.isSimpleMatchPattern(fField)) {
                            for (String field : context.mapperService().simpleMatchToIndexNames(fField)) {
                                settings.fields().add(field);
                                if (fBoost != -1) {
                                    if (settings.boosts() == null) {
                                        settings.boosts(new ObjectFloatOpenHashMap<String>());
                                    }
                                    settings.boosts().put(field, fBoost);
                                }
                            }
                        } else {
                            settings.fields().add(fField);
                            if (fBoost != -1) {
                                if (settings.boosts() == null) {
                                    settings.boosts(new ObjectFloatOpenHashMap<String>());
                                }
                                settings.boosts().put(fField, fBoost);
                            }
                        }
                    }
                } else {
                    throw new QueryParsingException(context.index(), "[so_query] query does not support [" + currentFieldName + "]");
                }
            } else if (token.isValue()) {
                if ("query".equals(currentFieldName)) {
                    settings.queryString(parser.text());
                } else if ("default_field".equals(currentFieldName) || "defaultField".equals(currentFieldName)) {
                    settings.defaultField(parser.text());
                } else if ("default_operator".equals(currentFieldName) || "defaultOperator".equals(currentFieldName)) {
                    String op = parser.text();
                    if ("or".equalsIgnoreCase(op)) {
                        //settings.defaultOperator(org.apache.lucene.queryparser.classicQueryParser.Operator.OR);
                        settings.defaultOperator(jp.scaleout.elasticsearch.plugins.queryparser.classic.QueryParser.Operator.OR);
                    } else if ("and".equalsIgnoreCase(op)) {
                        //settings.defaultOperator(org.apache.lucene.queryparser.classic.QueryParser.Operator.AND);
                        settings.defaultOperator(jp.scaleout.elasticsearch.plugins.queryparser.classic.QueryParser.Operator.AND);
                    } else {
                        throw new QueryParsingException(context.index(), "Query default operator [" + op + "] is not allowed");
                    }
                } else if ("analyzer".equals(currentFieldName)) {
                    NamedAnalyzer analyzer = context.analysisService().analyzer(parser.text());
                    if (analyzer == null) {
                        throw new QueryParsingException(context.index(), "[so_query] analyzer [" + parser.text() + "] not found");
                    }
                    settings.forcedAnalyzer(analyzer);
                } else if ("quote_analyzer".equals(currentFieldName) || "quoteAnalyzer".equals(currentFieldName)) {
                    NamedAnalyzer analyzer = context.analysisService().analyzer(parser.text());
                    if (analyzer == null) {
                        throw new QueryParsingException(context.index(), "[so_query] quote_analyzer [" + parser.text() + "] not found");
                    }
                    settings.forcedQuoteAnalyzer(analyzer);
                } else if ("auto_generate_phrase_queries".equals(currentFieldName) || "autoGeneratePhraseQueries".equals(currentFieldName)) {
                    settings.autoGeneratePhraseQueries(parser.booleanValue());
                } else if ("max_determinized_states".equals(currentFieldName) || "maxDeterminizedStates".equals(currentFieldName)) {
                    settings.maxDeterminizedStates(parser.intValue());
                } else if ("lowercase_expanded_terms".equals(currentFieldName) || "lowercaseExpandedTerms".equals(currentFieldName)) {
                    settings.lowercaseExpandedTerms(parser.booleanValue());
                } else if ("enable_position_increments".equals(currentFieldName) || "enablePositionIncrements".equals(currentFieldName)) {
                    settings.enablePositionIncrements(parser.booleanValue());
                } else if ("escape".equals(currentFieldName)) {
                    settings.escape(parser.booleanValue());
                } else if ("use_dis_max".equals(currentFieldName) || "useDisMax".equals(currentFieldName)) {
                    settings.useDisMax(parser.booleanValue());
                } else if ("phrase_slop".equals(currentFieldName) || "phraseSlop".equals(currentFieldName)) {
                    settings.phraseSlop(parser.intValue());
                } else if ("boost".equals(currentFieldName)) {
                    settings.boost(parser.floatValue());
                } else if ("tie_breaker".equals(currentFieldName) || "tieBreaker".equals(currentFieldName)) {
                    settings.tieBreaker(parser.floatValue());
                } else if ("rewrite".equals(currentFieldName)) {
                    settings.rewriteMethod(QueryParsers.parseRewriteMethod(parser.textOrNull()));
                } else if ("minimum_should_match".equals(currentFieldName) || "minimumShouldMatch".equals(currentFieldName)) {
                    settings.minimumShouldMatch(parser.textOrNull());
                } else if ("quote_field_suffix".equals(currentFieldName) || "quoteFieldSuffix".equals(currentFieldName)) {
                    settings.quoteFieldSuffix(parser.textOrNull());
                } else if ("lenient".equalsIgnoreCase(currentFieldName)) {
                    settings.lenient(parser.booleanValue());
                } else if ("locale".equals(currentFieldName)) {
                    String localeStr = parser.text();
                    settings.locale(LocaleUtils.parse(localeStr));
                } else if ("_name".equals(currentFieldName)) {
                    queryName = parser.text();
                } else if ("enable_negative_query".equals(currentFieldName)) {
		    settings.enableNegativeQuery(parser.booleanValue());
		} else if ("use_field_refine".equals(currentFieldName)) {
                    settings.useFieldRefine(parser.booleanValue());
                } else {
                    throw new QueryParsingException(context.index(), "[so_query] query does not support [" + currentFieldName + "]");
                }
            }
        }
        if (settings.queryString() == null) {
            throw new QueryParsingException(context.index(), "so_query must be provided with a [query]");
        }
        settings.defaultAnalyzer(context.mapperService().searchAnalyzer());
        settings.defaultQuoteAnalyzer(context.mapperService().searchQuoteAnalyzer());

        if (settings.escape()) {
            settings.queryString(jp.scaleout.elasticsearch.plugins.queryparser.classic.QueryParser.escape(settings.queryString()));
        }

        settings.queryTypes(context.queryTypes());
        Query query = null;
        MapperQueryParser queryParser = new MapperQueryParser(settings, context);
        
        try {
	    String q = settings.queryString();
	    q = Normalizer.normalize(q, Normalizer.Form.NFKC);
	    q = q.replaceAll("\\s+", " ");
            /*
	      q = q.replaceAll("\\s+", "\u0001");
	      q = q.replaceAll("\u0001(\\-\\S+)\u0001?", " $1 ");
	      q = q.replaceAll("\u0001OR\u0001?", " OR ");
	    */
	    //System.out.println("*** query: " + q);
	    settings.queryString(q);
	    boolean is_retry = false;
            do {
                try {
		    if (is_retry == true) {
			q = settings.queryString();
                        q = q.replace("\"", "");
                        q = q.replace("OR", "or");
			settings.queryString(q);
                        //System.out.println("*** query: " + q);
                    }
                    query = queryParser.parse(settings.queryString());
                    if (query == null) {
                        return null;
                    }
                    is_retry = false;
                } catch(jp.scaleout.elasticsearch.plugins.queryparser.classic.ParseException e) {
                    if (is_retry == true) {
                        throw e;
                    }
                    is_retry = true;
                }
            } while(is_retry);

            if (settings.boost() != QueryParserSettings.DEFAULT_BOOST) {
                query.setBoost(query.getBoost() * settings.boost());
            }
	    if (settings.enableNegativeQuery()) {
		query = fixNegativeQueryIfNeeded(query);
	    }
            if (query instanceof BooleanQuery) {
                Queries.applyMinimumShouldMatch((BooleanQuery) query, settings.minimumShouldMatch());
            }
            /*context.queryParserCache().put(settings, query);
	      if (queryName != null) {
	      context.addNamedQuery(queryName, query);
	      }*/
	    //System.out.println("query: " + query.toString());
            return query;
        } catch (jp.scaleout.elasticsearch.plugins.queryparser.classic.ParseException e) {
            throw new QueryParsingException(context.index(), "Failed to parse query [" + settings.queryString() + "]", e);
        }
    }
}
