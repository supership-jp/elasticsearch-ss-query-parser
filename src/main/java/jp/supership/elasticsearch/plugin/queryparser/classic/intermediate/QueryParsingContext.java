/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.classic.intermediate;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.queryparser.flexible.standard.CommonQueryParserConfiguration;

/**
 * This interface specifies the implementing class can be configured in accordance with the
 * Lucene's {@code CommonQueryParserConfiguration} API and some additional context.
 *
 * @author Shingo OKAWA
 * @since  08/11/2015
 */
public interface QueryParsingContext extends CommonQueryParserConfiguration {
    /**
     * Holds pre-defined operators.
     */
    public static interface Operators {
	public static final Operator AND = Operator.AND;
	public static final Operator OR  = Operator.OR;
    }

    /**
     * Sets the default field.
     * @param field the default field to be set.
     */
    public void setDefaultField(String field);

    /**
     * Returns the default field.
     * @return the assigned default field.
     */
    public String getDefaultField();

    /**
     * Sets the boolean operator for the query parser.
     * In default, <code>Operators.OR</code> is set, i.e., terms without any modifiers are considered optional:
     * for example <code>capital of Hungary</code> is equal to <code>capital OR of OR Hungary</code>.<br/>
     * @param operator the boolean operator to be set.
     */
    public void setDefaultOperator(Operator operator);

    /**
     * Returns the default boolean operator, which will be either <code>Operators.AND</code> or <code>Operators.OR</code>.
     * @return the assigned default boolean operator.
     */
    public Operator getDefaultOperator();

    /**
     * {@link PhraseQuery}s will be automatically generated when the analyzer returns more than one term
     * from whitespace-delimited text, if this value is set to be true. This behavior may not be
     * appropriate for some languages.
     * @param value the value to be set.
     */
    public void setPhraseQueryAutoGeneration(boolean value);

    /**
     * Returns the configured value of the phase-query-auto-generation functionality.
     * @return the assigned value of phase-query-auto-generation functionality.
     */
    public boolean getPhraseQueryAutoGeneration();

    /**
     * Sets the date resolution used by range queries for a specific field.
     * @param field      field for which the date resolution is to be set.
     * @param resolution date resolution to set.
     */
    public void setDateResolution(String field, DateTools.Resolution resolution);

    /**
     * Returns the date resolution that is used by RangeQueries for the given field.
     * @return null if no default or field specific date resolution has been set for the given field.
     */
    public DateTools.Resolution getDateResolution(String field);

    /**
     * {@link TermRangeQuery}s will be analyzed if this value is set to be true.
     * For example, setting this to true can enable analyzing terms into collation keys for locale-sensitive
     * {@link TermRangeQuery}.
     * @param analyzeRangeTerms whether or not terms should be analyzed for RangeQuerys
     */
    public void setRangeTermAnalysis(boolean value);

    /**
     * Returns the configured value of the range-term-analysis functionality.
     * @return whether or not to analyze range terms when constructing {@link TermRangeQuery}s.
     */
    public boolean getRangeTermAnalysis();

    /**
     * Sets the maximum number of states that determinizing a regexp query can result in.
     * @param max the maximum number of states to be set.
     */
    public void setMaxDeterminizedStates(int max);

    /**
     * Returns the configured maximum number of states.
     * @return the maximum number of states that determinizing a regexp query can result in.
     */
    public int getMaxDeterminizedStates();
}
