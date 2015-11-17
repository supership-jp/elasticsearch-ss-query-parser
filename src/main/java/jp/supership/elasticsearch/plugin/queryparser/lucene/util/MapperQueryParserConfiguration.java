/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
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
public class MapperQueryParserConfiguration extends DefaultQueryParserConfiguration {
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
    private Analyzer defaultQuotedAnalyzer = null;

    /** Holds enforced analyzer. */
    private Analyzer forcedAnalyzer = null;

    /** Holds enforced quoted analyzer. */
    private Analyzer forcedQuotedAnalyzer = null;

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
     * Returns the cachability of the current query.
     * @return true if the currently handling query is supposed to be cachable.
     */
    public boolean isCacheable() {
        // a hack for now :) to determine if a query string is cacheable
        return !queryString.contains("now");
    }

    /**
     * Returns the query string.
     * @return the assigned query string.
     */
    public String getQueryString() {
        return this.queryString;
    }

    /**
     * Sets the currently handling query string.
     * @param queryString the query string to be set.
     */
    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    /**
     * Returns the assigned boosting coeeficient.
     * @return the assigned boost value.
     */
    public float getBoost() {
        return this.boost;
    }

    /**
     * Sets the boosting coefficent.
     * @param boost the boosting coefficient to be set.
     */
    public void setBoost(float boost) {
        this.boost = boost;
    }

    /**
     * Returns the maximum fuzzy query expansion number.
     * @return the assigned maximum fuzzy query expansion number.
     */
    public int getFuzzyMaxExpansions() {
        return this.fuzzyMaxExpansions;
    }

    /**
     * Sets the maximum fuzzy query expansion number.
     * @param fuzzyMaxExpansions the maximum fuzzy query expansion number to be set.
     */
    public void setFuzzyMaxExpansions(int fuzzyMaxExpansions) {
        this.fuzzyMaxExpansions = fuzzyMaxExpansions;
    }

    /**
     * Returns the fuzzy query rewrite method.
     * @return the assigned fuzzy query rewrite method.
     */
    public MultiTermQuery.RewriteMethod getFuzzyRewriteMethod() {
        return this.fuzzyRewriteMethod;
    }

    /**
     * Sets the fuzzy query rewrite method.
     * @param fuzzyRewriteMethod the fuzzy query rewirte method to be set.
     */
    public void setFuzzyRewriteMethod(MultiTermQuery.RewriteMethod fuzzyRewriteMethod) {
        this.fuzzyRewriteMethod = fuzzyRewriteMethod;
    }

    /**
     * Returns the wildcard-analysis functionality setting.
     * @return the assigned wildcard-analysis functionality setting.
     */
    public boolean getWildcardAnalysis() {
        return this.wildcardAnalysis;
    }

    /**
     * Sets the wildcard-analysis functionality setting.
     * @param wildcardAnalysis the wildcard-analysis functionality setting to be set.
     */
    public void setWildcardAnalysis(boolean wildcardAnalysis) {
        this.wildcardAnalysis = wildcardAnalysis;
    }

    /**
     * Returns the escape functionality setting.
     * @return the assigned escape functionality setting.
     */
    public boolean getEscape() {
        return this.escape;
    }

    /**
     * Sets the escape functionality setting.
     * @param escape the escape functionality setting to be set.
     */
    public void setEscape(boolean escape) {
        this.escape = escape;
    }

    /**
     * Returns the assigned default analyzer.
     * @return the assigned default analyzer.
     */
    public Analyzer getDefaultAnalyzer() {
        return this.defaultAnalyzer;
    }

    /**
     * Sets the default analyzer.
     * @param defaultAnalyzer the default analyzer to be set.
     */
    public void setDefaultAnalyzer(Analyzer defaultAnalyzer) {
        this.defaultAnalyzer = defaultAnalyzer;
    }

    /**
     * Returns the assigned default quoted analyzer.
     * @return the assigned default quoted analyzer.
     */
    public Analyzer getDefaultQuotedAnalyzer() {
        return this.defaultQuotedAnalyzer;
    }

    /**
     * Sets the default quoted analyzer.
     * @param defaultQuotedAnalyzer the default quoted analyzer to be set.
     */
    public void setDefaultQuotedAnalyzer(Analyzer defaultQuotedAnalyzer) {
        this.defaultQuotedAnalyzer = defaultQuotedAnalyzer;
    }

    /**
     * Returns the assigned enforced analyzer.
     * @return the assigned enforced analyzer.
     */
    public Analyzer getForcedAnalyzer() {
        return this.forcedAnalyzer;
    }

    /**
     * Sets the forced analyzer.
     * @param forcedAnalyzer the forced analyzer to be set.
     */
    public void setForcedAnalyzer(Analyzer forcedAnalyzer) {
        this.forcedAnalyzer = forcedAnalyzer;
    }

    /**
     * Returns the assigned enforced quoted analyzer.
     * @return the assigned enforced analyzer.
     */
    public Analyzer getForcedQuotedAnalyzer() {
        return this.forcedQuotedAnalyzer;
    }

    /**
     * Sets the forced quoted analyzer.
     * @param forcedQuotedAnalyzer the forced quoted analyzer to be set.
     */
    public void setForcedQuotedAnalyzer(Analyzer forcedQuotedAnalyzer) {
        this.forcedQuotedAnalyzer = forcedQuotedAnalyzer;
    }

    /**
     * Returns the assigned quote field suffix.
     * @return the assigned quote field suffix.
     */
    public String getQuoteFieldSuffix() {
        return this.quoteFieldSuffix;
    }

    /**
     * Sets the quote field suffix.
     * @param quoteFieldSuffix the quote field suffix to be set.
     */
    public void setQuoteFieldSuffix(String quoteFieldSuffix) {
        this.quoteFieldSuffix = quoteFieldSuffix;
    }

    /**
     * Returns the rewrite method.
     * @return the assigned rewrite method.
     */
    public MultiTermQuery.RewriteMethod getRewriteMethod() {
        return this.rewriteMethod;
    }

    /**
     * Sets the rewrite method.
     * @param rewriteMethod the rewrite method to be set.
     */
    public void setRewriteMethod(MultiTermQuery.RewriteMethod rewriteMethod) {
        this.rewriteMethod = rewriteMethod;
    }

    /**
     * Returns the string to be used in should-match clause.
     * @return the assigned string to be used in should-match clause.
     */
    public String getMinimumShouldMatch() {
        return this.minimumShouldMatch;
    }

    /**
     * Sets the string to be used in should-match clause.
     * @param minimumShouldMatch the string to be set.
     */
    public void setMinimumShouldMatch(String minimumShouldMatch) {
        this.minimumShouldMatch = minimumShouldMatch;
    }

    /**
     * Returns the lenienet setting.
     * @return the assigned lenient setting.
     */
    public boolean getLenient() {
        return this.lenient;
    }

    /**
     * Sets the lenient setting.
     * @param lenient the lenient setting to be set.
     */
    public void setLenient(boolean lenient) {
        this.lenient = lenient;
    }

    /**
     * Returns the field refinement setting.
     * @return the assigned field refinement setting.
     */
    public boolean getFieldRefinement() {
        return this.fieldRefinement;
    }

    /**
     * Sets the field refinement setting.
     * @param fieldRefinement the field refinement setting to be set.
     */
    public void setFieldRefinement(boolean fieldRefinement) {
        this.fieldRefinement = fieldRefinement;
    }

    /**
     * Returns the query negation setting.
     * @return the assigned query negation setting.
     */
    public boolean getQueryNegation() {
	return this.queryNegation;
    }

    /**
     * Sets the query negation setting.
     * @param queryNegation the query negation setting to be set.
     */
    public void setQueryNegation(boolean queryNegation) {
	this.queryNegation = queryNegation;
    }

    /**
     * Returns the whole handling fields.
     * @return the assigned whole handling fields.
     */
    public List<String> getFields() {
        return this.fields;
    }

    /**
     * Sets the whole handling fields.
     * @param fields the whole handling fields to be set.
     */
    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    /**
     * Returns the currently handling query types.
     * @return the assigned currently handling query types.
     */
    public Collection<String> getQueryTypes() {
        return this.queryTypes;
    }

    /**
     * Sets the currently handling query types.
     * @param queryTypes the currently handling query types to be set.
     */
    public void setQueryTypes(Collection<String> queryTypes) {
        this.queryTypes = queryTypes;
    }

    /**
     * Returns the currently handling boosting coefficients.
     * @return the assigned currently handling boosting coefficients.
     */
    public ObjectFloatOpenHashMap<String> getBoosts() {
        return this.boosts;
    }

    /**
     * Sets the currently handling boosting coefficients.
     * @param boosts the currently handling boosting coefficients.
     */
    public void setBoosts(ObjectFloatOpenHashMap<String> boosts) {
        this.boosts = boosts;
    }

    /**
     * Returns the tie-break value.
     * @return the assigned tie-break value.
     */
    public float getTieBreaker() {
        return this.tieBreaker;
    }

    /**
     * Sets the tie-break value.
     * @param tieBreaker the tie-break value to be set.
     */
    public void setTieBreaker(float tieBreaker) {
        this.tieBreaker = tieBreaker;
    }

    /**
     * Returns the disjunction-max-query generation functionality setting.
     * @return the assigned disjunction-max-query generation functionality setting.
     */
    public boolean getUseDisMax() {
        return this.useDisMax;
    }

    /**
     * Sets the disjunction-max-query generation functionality setting.
     * @param useDisMax the disjunction-max-query generation functionality setting to be set.
     */
    public void useDisMax(boolean useDisMax) {
        this.useDisMax = useDisMax;
    }
}
