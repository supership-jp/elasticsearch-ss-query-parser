/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.config;

import java.util.Collection;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.util.automaton.Operations;
import org.elasticsearch.common.hppc.ObjectFloatOpenHashMap;
import org.joda.time.DateTimeZone;

/**
 * This class represents parsing contex, i.e., parser settings for the Elasticsearch query DSL tailered for Supership, inc.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class DSQParserSettings extends QueryParserSettings implements DSQParserConfiguration {
    /** Holds default boosting coefficient. */
    public static final float DEFAULT_BOOST = 1.0f;

    /** Holds currently handling query string. */
    private String queryString;

    /** Holds boosting value. */
    private float boost = DEFAULT_BOOST;

    /** Holds maximum number of fuzzy query expansion. */
    private int fuzzyMaxExpansions = FuzzyQuery.defaultMaxExpansions;

    /** Holds fuzzy-query-rewrite-method functionality setting. */
    private MultiTermQuery.RewriteMethod fuzzyRewriteMethod = null;

    /** Holds wildcard-analysis functionality setting. */
    private boolean wildcardAnalysis = DEFAULT_WILDCARD_ANALYSIS;

    /** Holds escape functionality setting. */
    private boolean escape = DEFAULT_ESCAPE;

    /** Holds default analyzer. */
    private Analyzer defaultAnalyzer = null;

    /** Holds default quoted analyzer. */
    private Analyzer defaultQuoteAnalyzer = null;

    /** Holds enforced analyzer. */
    private Analyzer forcedAnalyzer = null;

    /** Holds enforced quoted analyzer. */
    private Analyzer forcedQuoteAnalyzer = null;

    /** Holds quote-field-suffix. */
    private String quoteFieldSuffix = null;

    /** Holds default rewrite method. */
    private MultiTermQuery.RewriteMethod rewriteMethod = MultiTermQuery.CONSTANT_SCORE_FILTER_REWRITE;

    /** Holds string to be used for should-match. */
    private String minimumShouldMatch;

    /** Holds lenient setting. */
    private boolean lenient;

    /** Holds the field refinement functionality setting. */
    private boolean fieldRefinement = DEFAULT_FIELD_REFINEMENT;

    /** Holds the query negation functionality setting. */
    private boolean queryNegation = DEFAULT_QUERY_NEGATION;

    /** Holds the whole handling fields. */
    List<String> fields = null;

    /** Holds the currently handling query types. */
    Collection<String> queryTypes = null;

    /** Holds the currently handling boosting coefficients. */
    ObjectFloatOpenHashMap<String> boosts = null;

    /** Holds the tie-break value. */
    float tieBreaker = DEFAULT_TIE_BREAKER;

    /** Holds the disjunction-max-query generation functionaliuty setting. */
    boolean useDisMax = DEFAULT_USE_DISJUNCTION_MAX;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCacheable() {
        // a hack for now :) to determine if a query string is cacheable
        return !queryString.contains("now");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getQueryString() {
        return this.queryString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getBoost() {
        return this.boost;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBoost(float boost) {
        this.boost = boost;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getFuzzyMaxExpansions() {
        return this.fuzzyMaxExpansions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFuzzyMaxExpansions(int fuzzyMaxExpansions) {
        this.fuzzyMaxExpansions = fuzzyMaxExpansions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultiTermQuery.RewriteMethod getFuzzyRewriteMethod() {
        return this.fuzzyRewriteMethod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFuzzyRewriteMethod(MultiTermQuery.RewriteMethod fuzzyRewriteMethod) {
        this.fuzzyRewriteMethod = fuzzyRewriteMethod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getWildcardAnalysis() {
        return this.wildcardAnalysis;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWildcardAnalysis(boolean wildcardAnalysis) {
        this.wildcardAnalysis = wildcardAnalysis;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getEscape() {
        return this.escape;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEscape(boolean escape) {
        this.escape = escape;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Analyzer getDefaultAnalyzer() {
        return this.defaultAnalyzer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultAnalyzer(Analyzer defaultAnalyzer) {
        this.defaultAnalyzer = defaultAnalyzer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Analyzer getDefaultQuoteAnalyzer() {
        return this.defaultQuoteAnalyzer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaultQuoteAnalyzer(Analyzer defaultQuoteAnalyzer) {
        this.defaultQuoteAnalyzer = defaultQuoteAnalyzer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Analyzer getForcedAnalyzer() {
        return this.forcedAnalyzer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setForcedAnalyzer(Analyzer forcedAnalyzer) {
        this.forcedAnalyzer = forcedAnalyzer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Analyzer getForcedQuoteAnalyzer() {
        return this.forcedQuoteAnalyzer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setForcedQuoteAnalyzer(Analyzer forcedQuoteAnalyzer) {
        this.forcedQuoteAnalyzer = forcedQuoteAnalyzer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getQuoteFieldSuffix() {
        return this.quoteFieldSuffix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setQuoteFieldSuffix(String quoteFieldSuffix) {
        this.quoteFieldSuffix = quoteFieldSuffix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultiTermQuery.RewriteMethod getRewriteMethod() {
        return this.rewriteMethod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRewriteMethod(MultiTermQuery.RewriteMethod rewriteMethod) {
        this.rewriteMethod = rewriteMethod;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMinimumShouldMatch() {
        return this.minimumShouldMatch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMinimumShouldMatch(String minimumShouldMatch) {
        this.minimumShouldMatch = minimumShouldMatch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getLenient() {
        return this.lenient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLenient(boolean lenient) {
        this.lenient = lenient;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getFieldRefinement() {
        return this.fieldRefinement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFieldRefinement(boolean fieldRefinement) {
        this.fieldRefinement = fieldRefinement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getQueryNegation() {
	return this.queryNegation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setQueryNegation(boolean queryNegation) {
	this.queryNegation = queryNegation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getFields() {
        return this.fields;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getQueryTypes() {
        return this.queryTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setQueryTypes(Collection<String> queryTypes) {
        this.queryTypes = queryTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectFloatOpenHashMap<String> getBoosts() {
        return this.boosts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBoosts(ObjectFloatOpenHashMap<String> boosts) {
        this.boosts = boosts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getTieBreaker() {
        return this.tieBreaker;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTieBreaker(float tieBreaker) {
        this.tieBreaker = tieBreaker;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getUseDisMax() {
        return this.useDisMax;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUseDisMax(boolean useDisMax) {
        this.useDisMax = useDisMax;
    }
}
