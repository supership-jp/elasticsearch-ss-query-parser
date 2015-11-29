/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ExistsFieldQueryExtension;
import org.apache.lucene.queryparser.classic.MissingFieldQueryExtension;
import org.apache.lucene.queryparser.classic.FieldQueryExtension;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.MultiTermQuery;
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
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.config.DSQParserConfiguration;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.config.DSQParserSettings;
import static java.util.Collections.unmodifiableMap;
import static org.elasticsearch.common.lucene.search.Queries.fixNegativeQueryIfNeeded;
import static jp.supership.elasticsearch.plugin.queryparser.lucene.util.config.QueryParserConfiguration.Wildcard;

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

    static {
	Map<String, FieldQueryExtension> fieldQueryExtensions = new HashMap<>();
	fieldQueryExtensions.put(ExistsFieldQueryExtension.NAME, new ExistsFieldQueryExtension());
	fieldQueryExtensions.put(MissingFieldQueryExtension.NAME, new MissingFieldQueryExtension());
	FIELD_QUERY_EXTENSIONS = unmodifiableMap(fieldQueryExtensions);
    }

    /** Holds true if the configuration specifies the current context uses forced analyzer. */
    private boolean enforcingAnalyzer;

    /** Holds true if the configuration specifies the current context uses forced quoted analyzer. */
    private boolean enforcingQuoteAnalyzer;

    /** Holds an analyzer to be used in quoted term. */
    private Analyzer quoteAnalyzer;

    /** Holds currently handling mapper. */
    private FieldMapper currentMapper;

    /**
     * Constructor.
     */
    public MapperQueryEngine() {
	super();
    }

    /**
     * Constructor.
     */
    public MapperQueryEngine(QueryParseContext context) {
	this.context = context;
	this.configure(new DSQParserSettings());
    }

    /**
     * Constructor.
     */
    public MapperQueryEngine(QueryParseContext context, DSQParserConfiguration configuration) {
        this.context = context;
	this.configure(configuration);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void configure(DSQParserConfiguration configuration) {
        this.configuration = configuration;
  
        if (configuration.getFields() != null && configuration.getFields().size() == 1) {
	    this.setDefaultField(configuration.getFields().get(0));
        }

        this.enforcingAnalyzer = configuration.getForcedAnalyzer() != null;
        this.setAnalyzer(this.enforcingAnalyzer ? configuration.getForcedAnalyzer() : configuration.getDefaultAnalyzer());

        if (configuration.getForcedQuoteAnalyzer() != null) {
            this.enforcingQuoteAnalyzer = true;
            this.setQuoteAnalyzer(configuration.getForcedQuoteAnalyzer());
        } else if (this.enforcingAnalyzer) {
            this.enforcingQuoteAnalyzer = true;
            this.setQuoteAnalyzer(configuration.getForcedAnalyzer());
        } else {
            this.enforcingAnalyzer = false;
            this.setQuoteAnalyzer(configuration.getDefaultQuoteAnalyzer());
        }

        this.setMultiTermRewriteMethod(configuration.getRewriteMethod());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Query newTermQuery(Term term) {
        if (this.currentMapper != null) {
            Query termQuery = this.currentMapper.queryStringTermQuery(term);
            if (termQuery != null) {
                return termQuery;
            }
        }
        return super.newTermQuery(term);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Query newMatchAllDocsQuery() {
        return Queries.newMatchAllQuery();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query getFieldQuery(String field, String queryText, boolean quoted) throws ParseException {
        if (this.getFieldRefinement() == false || (this.getFields() != null && this.getFields().size() > 0)) {
            field = null;
        }

        FieldQueryExtension fieldQueryExtension = FIELD_QUERY_EXTENSIONS.get(field);
        if (fieldQueryExtension != null) {
            return fieldQueryExtension.query(this.context, queryText);
        }

        Collection<String> fields = this.extractMultiFields(field);
        if (fields != null) {
	    // If the bundling fields are unique, it must be handled as signle field query.
            if (fields.size() == 1) {
                return this.getSingleFieldQuery(fields.iterator().next(), queryText, quoted);
            }

	    // If this engine was configured to use disjunction max query, then go for it.
            if (this.getUseDisMax()) {
                DisjunctionMaxQuery disMaxQuery = new DisjunctionMaxQuery(this.getTieBreaker());
                boolean added = false;
                for (String current : fields) {
                    Query query = this.getSingleFieldQuery(current, queryText, quoted);
                    if (query != null) {
                        added = true;
                        this.boostify(query, current);
                        disMaxQuery.add(query);
                    }
                }
                if (!added) {
                    return null;
                }
                return disMaxQuery;
            // If this engine was configured not to use disjunction max query, conjugate queries as Booealn.
            } else {
                List<BooleanClause> clauses = new ArrayList<>();
                for (String current : fields) {
                    Query query = this.getSingleFieldQuery(current, queryText, quoted);
                    if (query != null) {
                        this.boostify(query, current);
                        clauses.add(new BooleanClause(query, BooleanClause.Occur.SHOULD));
                    }
                }
                if (clauses.size() == 0) {
                    return null;
		}
                return this.getBooleanQuery(clauses, true);
            }
        } else {
            return this.getSingleFieldQuery(field, queryText, quoted);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query getFieldQuery(String field, String queryText, int slop) throws ParseException {
        Collection<String> fields = this.extractMultiFields(field);
        if (fields != null) {
	    // If this engine was configured to use disjunction max query, then go for it.
            if (this.getUseDisMax()) {
                DisjunctionMaxQuery disMaxQuery = new DisjunctionMaxQuery(this.getTieBreaker());
                boolean added = false;
                for (String current : fields) {
                    Query query = super.getFieldQuery(current, queryText, slop);
                    if (query != null) {
                        added = true;
                        this.boostify(query, current);
                        this.slopify(query, slop);
                        disMaxQuery.add(query);
                    }
                }
                if (!added) {
                    return null;
                }
                return disMaxQuery;
            // If this engine was configured not to use disjunction max query, conjugate queries as Booealn.
            } else {
                List<BooleanClause> clauses = new ArrayList<>();
                for (String current : fields) {
                    Query query = super.getFieldQuery(current, queryText, slop);
                    if (query != null) {
                        this.boostify(query, current);
                        this.slopify(query, slop);
                        clauses.add(new BooleanClause(query, BooleanClause.Occur.SHOULD));
                    }
                }
                if (clauses.size() == 0) {
                    return null;
		}
                return this.getBooleanQuery(clauses, true);
            }
        } else {
            return super.getFieldQuery(field, queryText, slop);
        }
    }

    /**
     * Returns single field's {@code FieldQuery} in accordance to the assigned configuration.
     * @param  field the currently handling field.
     * @param  queryText the currently handling raw query string.
     * @param  quoted this value must be ser true if the handling query is considered to be quoted.
     * @return the resulting {@code Query} instance.
     * @throws ParseException if the parsing fails.
     */
    private Query getSingleFieldQuery(String field, String queryText, boolean quoted) throws ParseException {
	this.setCurrentMapper(null);
        Analyzer previousAnalyzer = this.getAnalyzer();
        try {
            MapperService.SmartNameFieldMappers fieldMappers = null;
	    // An optional field name suffix to automatically try and add to the field searched when using quoted text.
            if (quoted) {
                this.setAnalyzer(this.getQuoteAnalyzer());
                if (this.getQuoteFieldSuffix() != null) {
                    fieldMappers = this.context.smartFieldMappers(field + this.getQuoteFieldSuffix());
                }
            }
	    // If no mappers found for suffixed field, try to find one for plain field name.
            if (fieldMappers == null) {
                fieldMappers = this.context.smartFieldMappers(field);
            }
	    // If some mappers were found for the fields, then apply mappers.
            if (fieldMappers != null) {
                if (quoted) {
                    if (!this.enforcingQuoteAnalyzer) {
                        this.setAnalyzer(fieldMappers.searchQuoteAnalyzer());
                    }
                } else {
                    if (!this.enforcingAnalyzer) {
                        this.setAnalyzer(fieldMappers.searchAnalyzer());
                    }
                }
                this.setCurrentMapper(fieldMappers.fieldMappers().mapper());
                if (this.getCurrentMapper() != null) {
                    Query query = null;
                    if (this.getCurrentMapper().useTermQueryWithQueryString()) {
                        try {
                            query = this.getCurrentMapper().termQuery(queryText, this.context);
                        } catch (RuntimeException exception) {
                            if (this.getLenient()) {
                                return null;
                            } else {
                                throw exception;
                            }
                        }
                    }
                    if (query == null) {
                        query = super.getFieldQuery(this.getCurrentMapper().names().indexName(), queryText, quoted);
                    }
                    return query;
                }
            }
            return super.getFieldQuery(field, queryText, quoted);
        } finally {
            this.setAnalyzer(previousAnalyzer);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query getRangeQuery(String field, String infinimum, String supremum, boolean leftInclusive, boolean rightInclusive) throws ParseException {
        if (Wildcard.STRING.toString().equals(infinimum)) {
            infinimum = null;
        }
        if (Wildcard.STRING.toString().equals(supremum)) {
            supremum = null;
        }

        Collection<String> fields = this.extractMultiFields(field);
        if (fields == null) {
            return this.getSingleRangeQuery(field, infinimum, supremum, leftInclusive, rightInclusive);
        }


        if (fields.size() == 1) {
            return this.getSingleRangeQuery(fields.iterator().next(), infinimum, supremum, leftInclusive, rightInclusive);
        }

	// If this engine was configured to use disjunction max query, then go for it.
        if (this.getUseDisMax()) {
            DisjunctionMaxQuery disMaxQuery = new DisjunctionMaxQuery(this.getTieBreaker());
            boolean added = false;
            for (String current : fields) {
                Query query = this.getSingleRangeQuery(current, infinimum, supremum, leftInclusive, rightInclusive);
                if (query != null) {
                    added = true;
                    this.boostify(query, current);
                    disMaxQuery.add(query);
                }
            }
            if (!added) {
                return null;
            }
            return disMaxQuery;
        // If this engine was configured not to use disjunction max query, conjugate queries as Booealn.
        } else {
            List<BooleanClause> clauses = new ArrayList<>();
            for (String current : fields) {
                Query query = this.getSingleRangeQuery(current, infinimum, supremum, leftInclusive, rightInclusive);
                if (query != null) {
                    this.boostify(query, current);
                    clauses.add(new BooleanClause(query, BooleanClause.Occur.SHOULD));
                }
            }
            if (clauses.size() == 0) {
                return null;
	    }
            return this.getBooleanQuery(clauses, true);
        }
    }

    /**
     * Returns single field's {@code TermRangeQuery} in accordance to the assigned configuration.
     * @param  field the currently handling field.
     * @param  infinimum the assigned range's infinimum.
     * @param  supremum the assigned range's supremum.
     * @param  leftInclusive true if the infinimum of the range is inclusive
     * @param  rightInclusive true if the supremum of the range is inclusive
     * @return new {@link TermRangeQuery} instance
     * @throws ParseException if the parsing fails.
     */
    private Query getSingleRangeQuery(String field, String infinimum, String supremum, boolean leftInclusive, boolean rightInclusive) throws ParseException {
        this.setCurrentMapper(null);
        MapperService.SmartNameFieldMappers fieldMappers = this.context.smartFieldMappers(field);

	// If some mappers were found for the fields, then apply mappers.
        if (fieldMappers != null) {
            this.setCurrentMapper(fieldMappers.fieldMappers().mapper());
            if (this.getCurrentMapper() != null) {
                if (this.getLowercaseExpandedTerms() && !this.getCurrentMapper().isNumeric()) {
                    infinimum = infinimum == null ? null : infinimum.toLowerCase(this.getLocale());
                    supremum = supremum == null ? null : supremum.toLowerCase(this.getLocale());
                }
                try {
                    return this.getCurrentMapper().rangeQuery(infinimum, supremum, leftInclusive, rightInclusive, this.context);
                } catch (RuntimeException exception) {
                    if (this.getLenient()) {
                        return null;
                    }
                    throw exception;
                }
            }
        }

        return this.newRangeQuery(field, infinimum, supremum, leftInclusive, rightInclusive);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query getPrefixQuery(String field, String termText) throws ParseException {
        if (this.getLowercaseExpandedTerms()) {
            termText = termText.toLowerCase(this.getLocale());
        }

        Collection<String> fields = this.extractMultiFields(field);
        if (fields != null) {
            if (fields.size() == 1) {
                return this.getSinglePrefixQuery(fields.iterator().next(), termText);
            }
	    // If this engine was configured to use disjunction max query, then go for it.
            if (this.getUseDisMax()) {
                DisjunctionMaxQuery disMaxQuery = new DisjunctionMaxQuery(this.getTieBreaker());
                boolean added = false;
                for (String current : fields) {
                    Query query = this.getSinglePrefixQuery(current, termText);
                    if (query != null) {
                        added = true;
                        this.boostify(query, current);
                        disMaxQuery.add(query);
                    }
                }
                if (!added) {
                    return null;
                }
                return disMaxQuery;
            // If this engine was configured not to use disjunction max query, conjugate queries as Booealn.
            } else {
                List<BooleanClause> clauses = new ArrayList<>();
                for (String current : fields) {
                    Query query = this.getSinglePrefixQuery(current, termText);
                    if (query != null) {
                        this.boostify(query, current);
                        clauses.add(new BooleanClause(query, BooleanClause.Occur.SHOULD));
                    }
                }
                if (clauses.size() == 0) {
                    return null;
		}
                return this.getBooleanQuery(clauses, true);
            }
        } else {
            return this.getSinglePrefixQuery(field, termText);
        }
    }

    /**
     * Returns single field's {@code PrefixQuery} in accordance to the assigned configuration.
     * Can be overridden by extending classes, to provide custom handling for
     * wildcard queries, which may be necessary due to missing analyzer calls.
     * @param  field the currently handling field.
     * @param  termText term to use for building term for the query without trailing '*'.
     * @return new {@link Query} instance.
     * @throws ParseException if the parsing fails.
     */
    private Query getSinglePrefixQuery(String field, String termText) throws ParseException {
        this.setCurrentMapper(null);
        Analyzer previousAnalyzer = getAnalyzer();

        try {
            MapperService.SmartNameFieldMappers fieldMappers = this.context.smartFieldMappers(field);
	    // If some mappers were found for the fields, then apply mappers.
            if (fieldMappers != null) {
                if (!this.enforcingAnalyzer) {
                    this.setAnalyzer(fieldMappers.searchAnalyzer());
                }
                this.setCurrentMapper(fieldMappers.fieldMappers().mapper());
                if (this.getCurrentMapper() != null) {
                    Query query = null;
                    if (this.getCurrentMapper().useTermQueryWithQueryString()) {
                        query = this.getCurrentMapper().prefixQuery(termText, this.getMultiTermRewriteMethod(), this.context);
                    }
                    if (query == null) {
                        query = this.getPossiblyAnalyzedPrefixQuery(this.getCurrentMapper().names().indexName(), termText);
                    }
                    return query;
                }
            }
            return this.getPossiblyAnalyzedPrefixQuery(field, termText);
        } catch (RuntimeException exception) {
            if (this.getLenient()) {
                return null;
            }
            throw exception;
        } finally {
            this.setAnalyzer(previousAnalyzer);
        }
    }

    /**
     * Returns possibly analyzed single field's {@code PrefixQuery} in accordance to the assigned configuration.
     * Can be overridden by extending classes, to provide custom handling for
     * wildcard queries, which may be necessary due to missing analyzer calls.
     * @param  field the currently handling field.
     * @param  termText term to use for building term for the query without trailing '*'.
     * @return new {@link Query} instance.
     * @throws ParseException if the parsing fails.
     */
    private Query getPossiblyAnalyzedPrefixQuery(String field, String termText) throws ParseException {
        if (!this.getWildcardAnalysis()) {
            return super.getPrefixQuery(field, termText);
        }

        // get Analyzer from superclass and tokenize the term.
        TokenStream source;
        try {
            source = this.getAnalyzer().tokenStream(field, termText);
            source.reset();
        } catch (IOException e) {
            return super.getPrefixQuery(field, termText);
        }

	// get tokens with the assigned analyzer.
        List<String> tokens = new ArrayList<>();
        CharTermAttribute attribute = source.addAttribute(CharTermAttribute.class);
        while (true) {
            try {
                if (!source.incrementToken()) break;
            } catch (IOException e) {
                break;
            }
            tokens.add(attribute.toString());
        }

        try {
            source.close();
        } catch (IOException e) {
            // DO NOTHING, this implementation is IOException safe.
        }

	// build a boolean query with prefix on the found one.
        if (tokens.size() == 1) {
            return super.getPrefixQuery(field, tokens.get(0));
        // build a boolean query with prefix on each one.
        } else {
            List<BooleanClause> clauses = new ArrayList<>();
            for (String token : tokens) {
                clauses.add(new BooleanClause(super.getPrefixQuery(field, token), BooleanClause.Occur.SHOULD));
            }
            return this.getBooleanQuery(clauses, true);
        }
    }

    /**
     * Extracts relating fields using {@code IndexQueryParser}'s internal {@code MapperService}.
     * @param  field the field name to be analyzed.
     * @return the relating field names' {@code Collection}
     */
    private Collection<String> extractMultiFields(String field) {
        Collection<String> fields = null;
        if (field != null) {
            fields = this.context.simpleMatchToIndexNames(field);
        } else {
            fields = this.getFields();
        }
        return fields;
    }

    /**
     * Boostifies the assigned query if it is contaied in the context.
     * @param query the query to be boostified.
     * @param field the field name to be analyzed.
     */
    private void boostify(Query query, String field) {
        if (this.getBoosts() != null) {
            float boost = 1.0f;
            if (this.getBoosts().containsKey(field)) {
                boost = this.getBoosts().lget();
            }
            query.setBoost(boost);
        }
    }

    /**
     * Slopifies the assigned query.
     * @param query the query to be slopified.
     * @param slop the slop distance to be allowed.
     */
    private void slopify(Query query, int slop) {
        if (query instanceof FilteredQuery) {
            this.slopify(((FilteredQuery)query).getQuery(), slop);
        }
        if (query instanceof PhraseQuery) {
            ((PhraseQuery) query).setSlop(slop);
        } else if (query instanceof MultiPhraseQuery) {
            ((MultiPhraseQuery) query).setSlop(slop);
        }
    }

    /**
     * Returns the analyzer to be used for quoted terms.
     * @return the assigned analyzer.
     */
    public Analyzer getQuoteAnalyzer() {
	return this.quoteAnalyzer;
    }

    /**
     * Sets the analyer to be used for quoted terms.
     * @param quoteAnalyzer the analyzer to be set.
     */
    public void setQuoteAnalyzer(Analyzer quoteAnalyzer) {
	this.quoteAnalyzer = quoteAnalyzer;
    }

    /**
     * Returns the currently handling {@code FieldMapper} instance.
     * @return the currently handling mapper.
     */
    public FieldMapper getCurrentMapper() {
	return this.currentMapper;
    }

    /**
     * Sets the currently handling {@code FieldMapper} instance.
     * @param currentMapper the mapper to be set.
     */
    public void setCurrentMapper(FieldMapper currentMapper) {
	this.currentMapper = currentMapper;
    }
}
