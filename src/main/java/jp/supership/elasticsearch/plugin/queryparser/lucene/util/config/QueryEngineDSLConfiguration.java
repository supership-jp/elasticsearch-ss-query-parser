/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.config;

import java.util.Collection;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.MultiTermQuery;
import org.elasticsearch.common.hppc.ObjectFloatOpenHashMap;

/**
 * This interface specifies the implementing class can be configured in accordance with the
 * Lucene's {@code CommonQueryParserConfiguration} API and some additional context.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface QueryEngineDSLConfiguration extends QueryEngineConfiguration {
        /**
     * Returns the cachability of the current query.
     * @return true if the currently handling query is supposed to be cachable.
     */
    public boolean isCacheable();

    /**
     * Returns the query string.
     * @return the assigned query string.
     */
    public String getQueryString();

    /**
     * Sets the currently handling query string.
     * @param queryString the query string to be set.
     */
    public void setQueryString(String queryString);

    /**
     * Returns the assigned boosting coeeficient.
     * @return the assigned boost value.
     */
    public float getBoost();

    /**
     * Sets the boosting coefficent.
     * @param boost the boosting coefficient to be set.
     */
    public void setBoost(float boost);

    /**
     * Returns the maximum fuzzy query expansion number.
     * @return the assigned maximum fuzzy query expansion number.
     */
    public int getFuzzyMaxExpansions();

    /**
     * Sets the maximum fuzzy query expansion number.
     * @param fuzzyMaxExpansions the maximum fuzzy query expansion number to be set.
     */
    public void setFuzzyMaxExpansions(int fuzzyMaxExpansions);

    /**
     * Returns the fuzzy query rewrite method.
     * @return the assigned fuzzy query rewrite method.
     */
    public MultiTermQuery.RewriteMethod getFuzzyRewriteMethod();

    /**
     * Sets the fuzzy query rewrite method.
     * @param fuzzyRewriteMethod the fuzzy query rewirte method to be set.
     */
    public void setFuzzyRewriteMethod(MultiTermQuery.RewriteMethod fuzzyRewriteMethod);

    /**
     * Returns the wildcard-analysis functionality setting.
     * @return the assigned wildcard-analysis functionality setting.
     */
    public boolean getWildcardAnalysis();

    /**
     * Sets the wildcard-analysis functionality setting.
     * @param wildcardAnalysis the wildcard-analysis functionality setting to be set.
     */
    public void setWildcardAnalysis(boolean wildcardAnalysis);

    /**
     * Returns the escape functionality setting.
     * @return the assigned escape functionality setting.
     */
    public boolean getEscape();

    /**
     * Sets the escape functionality setting.
     * @param escape the escape functionality setting to be set.
     */
    public void setEscape(boolean escape);

    /**
     * Returns the assigned default analyzer.
     * @return the assigned default analyzer.
     */
    public Analyzer getDefaultAnalyzer();

    /**
     * Sets the default analyzer.
     * @param defaultAnalyzer the default analyzer to be set.
     */
    public void setDefaultAnalyzer(Analyzer defaultAnalyzer);

    /**
     * Returns the assigned default quoted analyzer.
     * @return the assigned default quoted analyzer.
     */
    public Analyzer getDefaultQuoteAnalyzer();

    /**
     * Sets the default quoted analyzer.
     * @param defaultQuoteAnalyzer the default quoted analyzer to be set.
     */
    public void setDefaultQuoteAnalyzer(Analyzer defaultQuoteAnalyzer);

    /**
     * Returns the assigned enforced analyzer.
     * @return the assigned enforced analyzer.
     */
    public Analyzer getForcedAnalyzer();

    /**
     * Sets the forced analyzer.
     * @param forcedAnalyzer the forced analyzer to be set.
     */
    public void setForcedAnalyzer(Analyzer forcedAnalyzer);

    /**
     * Returns the assigned enforced quoted analyzer.
     * @return the assigned enforced analyzer.
     */
    public Analyzer getForcedQuoteAnalyzer();

    /**
     * Sets the forced quoted analyzer.
     * @param forcedQuoteAnalyzer the forced quoted analyzer to be set.
     */
    public void setForcedQuoteAnalyzer(Analyzer forcedQuoteAnalyzer);

    /**
     * Returns the assigned quote field suffix.
     * @return the assigned quote field suffix.
     */
    public String getQuoteFieldSuffix();

    /**
     * Sets the quote field suffix.
     * @param quoteFieldSuffix the quote field suffix to be set.
     */
    public void setQuoteFieldSuffix(String quoteFieldSuffix);

    /**
     * Returns the rewrite method.
     * @return the assigned rewrite method.
     */
    public MultiTermQuery.RewriteMethod getRewriteMethod();

    /**
     * Sets the rewrite method.
     * @param rewriteMethod the rewrite method to be set.
     */
    public void setRewriteMethod(MultiTermQuery.RewriteMethod rewriteMethod);

    /**
     * Returns the string to be used in should-match clause.
     * @return the assigned string to be used in should-match clause.
     */
    public String getMinimumShouldMatch();

    /**
     * Sets the string to be used in should-match clause.
     * @param minimumShouldMatch the string to be set.
     */
    public void setMinimumShouldMatch(String minimumShouldMatch);

    /**
     * Returns the lenienet setting.
     * @return the assigned lenient setting.
     */
    public boolean getLenient();

    /**
     * Sets the lenient setting.
     * @param lenient the lenient setting to be set.
     */
    public void setLenient(boolean lenient);

    /**
     * Returns the field refinement setting.
     * @return the assigned field refinement setting.
     */
    public boolean getFieldRefinement();

    /**
     * Sets the field refinement setting.
     * @param fieldRefinement the field refinement setting to be set.
     */
    public void setFieldRefinement(boolean fieldRefinement);

    /**
     * Returns the query negation setting.
     * @return the assigned query negation setting.
     */
    public boolean getQueryNegation();

    /**
     * Sets the query negation setting.
     * @param queryNegation the query negation setting to be set.
     */
    public void setQueryNegation(boolean queryNegation);

    /**
     * Returns the whole handling fields.
     * @return the assigned whole handling fields.
     */
    public List<String> getFields();

    /**
     * Sets the whole handling fields.
     * @param fields the whole handling fields to be set.
     */
    public void setFields(List<String> fields);

    /**
     * Returns the currently handling query types.
     * @return the assigned currently handling query types.
     */
    public Collection<String> getQueryTypes();

    /**
     * Sets the currently handling query types.
     * @param queryTypes the currently handling query types to be set.
     */
    public void setQueryTypes(Collection<String> queryTypes);

    /**
     * Returns the currently handling boosting coefficients.
     * @return the assigned currently handling boosting coefficients.
     */
    public ObjectFloatOpenHashMap<String> getBoosts();

    /**
     * Sets the currently handling boosting coefficients.
     * @param boosts the currently handling boosting coefficients.
     */
    public void setBoosts(ObjectFloatOpenHashMap<String> boosts);

    /**
     * Returns the tie-break value.
     * @return the assigned tie-break value.
     */
    public float getTieBreaker();

    /**
     * Sets the tie-break value.
     * @param tieBreaker the tie-break value to be set.
     */
    public void setTieBreaker(float tieBreaker);

    /**
     * Returns the disjunction-max-query generation functionality setting.
     * @return the assigned disjunction-max-query generation functionality setting.
     */
    public boolean getUseDisMax();

    /**
     * Sets the disjunction-max-query generation functionality setting.
     * @param useDisMax the disjunction-max-query generation functionality setting to be set.
     */
    public void setUseDisMax(boolean useDisMax);
}
