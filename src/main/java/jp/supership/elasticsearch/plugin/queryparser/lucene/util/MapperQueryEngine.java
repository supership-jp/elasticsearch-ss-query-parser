/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import com.google.common.base.Objects;
//import com.google.common.collect.ImmutableMap;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ExistsFieldQueryExtension;
import org.apache.lucene.queryparser.classic.MissingFieldQueryExtension;
//import org.apache.lucene.queryparser.classic.QueryParserSettings;
import org.apache.lucene.queryparser.classic.FieldQueryExtension;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.util.automaton.RegExp;
import org.elasticsearch.common.lucene.search.MatchNoDocsQuery;
import org.elasticsearch.common.lucene.search.Queries;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.mapper.FieldMapper;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.index.query.support.QueryParsers;
import static java.util.Collections.unmodifiableMap;
import static org.elasticsearch.common.lucene.search.Queries.fixNegativeQueryIfNeeded;

/**
 * This class is responsible for instanciating Lucene queries, query parser delegates all sub-query
 * instanciation tasks to this class. This implementation assumes that concrete query handler will be 
 * constructed in accordance to the given ANTLR grammar.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public abstract class MapperQueryEngine extends QueryEngine {
    /** Holds {@code FieldQueryExtension} which handle additional services such as caching. */
    public static final Map<String, FieldQueryExtension> FIELD_QUERY_EXTENSIONS;

    // Prepares for the service.
    static {
	Map<String, FieldQueryExtension> fieldQueryExtensions = new HashMap<>();
	fieldQueryExtensions.put(ExistsFieldQueryExtension.NAME, new ExistsFieldQueryExtension());
	fieldQueryExtensions.put(MissingFieldQueryExtension.NAME, new MissingFieldQueryExtension());
	FIELD_QUERY_EXTENSIONS = unmodifiableMap(fieldQueryExtensions);
    }

    /** Holds ES query parsing context. */
    //private final QueryParseContext context;
}
