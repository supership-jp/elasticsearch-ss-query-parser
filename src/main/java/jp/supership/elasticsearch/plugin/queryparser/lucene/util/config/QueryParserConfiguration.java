/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.config;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.queryparser.flexible.standard.CommonQueryParserConfiguration;

/**
 * This interface specifies the implementing class can be configured in accordance with the
 * Lucene's {@code CommonQueryParserConfiguration} API and some additional context.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface QueryParserConfiguration extends CommonQueryParserConfiguration {
    /** Holds default leading-wildcard functionality setting. */
    public static final boolean DEFAULT_ALLOW_LEADING_WILDCARD = true;

    /** Holds default phrase-query-generation functionality setting. */
    public static final boolean DEFAULT_PHRASE_QUERY_AUTO_GENERATION = false;

    /** Holds default lowercase-term-expansion functionality setting. */
    public static final boolean DEFAULT_LOWERCASE_TERM_EXPANSION = true;

    /** Holds default position-increments functionality setting. */
    public static final boolean DEFAULT_POSITION_INCREMENTS = true;

    /** Holds default analyzing-wildcard functionalit setting. */
    public static final boolean DEFAULT_WILDCARD_ANALYSIS = false;

    /** Holds default escape functionalit setting. */
    public static final boolean DEFAULT_ESCAPE = false;

    /** Holds default field refinement functionality setting. */
    public static final boolean DEFAULT_FIELD_REFINEMENT = true;

    /** Holds default queyr negation functionality setting. */
    public static final boolean DEFAULT_QUERY_NEGATION = false;

    /** Holds default disjunction-max-query generation functionality setting. */
    public static final boolean DEFAULT_USE_DISJUNCTION_MAX = true;

    /** Holds default analyuzing-wildcard functionalit setting. */
    public static final int DEFAULT_PHRASE_SLOP = 0;

    /** Holds default tie-break value. */
    public static final float DEFAULT_TIE_BREAKER = 0.0f;

    /**
     * Holds pre-defined wildcards.
     */
    public static enum Wildcard {
	NONE(""),
	STRING("*"),
	CHARACTER("?");

	// Holds actual string expression.
	private String expression;

	// Constructor.
	private Wildcard(String expression) {
	    this.expression = expression;
	}

	// Returns corresponding enum instance from the given expression.
	public static Wildcard find(String expression) {
	    for (Wildcard wildcard : Wildcard.values()) {
		if (expression.equals(wildcard.expression)) {
		    return wildcard;
		}
	    }
	    return Wildcard.NONE;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
	    return this.expression;
	}
    }

    /**
     * Sets the default field.
     * @param defaultField the default field to be set.
     */
    public void setDefaultField(String defaultField);

    /**
     * Returns the default field.
     * @return the assigned default field.
     */
    public String getDefaultField();

    /**
     * Sets the boolean operator for the query parser.
     * In default, <code>Operators.OR</code> is set, i.e., terms without any modifiers are considered optional:
     * for example <code>capital of Hungary</code> is equal to <code>capital OR of OR Hungary</code>.<br/>
     * @param defaultOperator the boolean operator to be set.
     */
    public void setDefaultOperator(int defaultOperator);

    /**
     * Returns the default boolean operator, which will be either <code>Operators.AND</code> or <code>Operators.OR</code>.
     * @return the assigned default boolean operator.
     */
    public int getDefaultOperator();

    /**
     * {@link PhraseQuery}s will be automatically generated when the analyzer returns more than one term
     * from whitespace-delimited text, if this value is set to be true. This behavior may not be
     * appropriate for some languages.
     * @param phraseQueryAutoGeneration the value to be set.
     */
    public void setPhraseQueryAutoGeneration(boolean phraseQueryAutoGeneration);

    /**
     * Returns the configured value of the phase-query-auto-generation functionality.
     * @return the assigned value of phase-query-auto-generation functionality.
     */
    public boolean getPhraseQueryAutoGeneration();

    /**
     * Sets the date resolution used by range queries for a specific field.
     * @param field          field for which the date resolution is to be set.
     * @param dateResolution date resolution to set.
     */
    public void setDateResolution(String field, DateTools.Resolution dateResolution);

    /**
     * Returns the date resolution that is used by RangeQueries for the given field.
     * @return null if no default or field specific date resolution has been set for the given field.
     */
    public DateTools.Resolution getDateResolution(String field);

    /**
     * {@link TermRangeQuery}s will be analyzed if this value is set to be true.
     * For example, setting this to true can enable analyzing terms into collation keys for locale-sensitive
     * {@link TermRangeQuery}.
     * @param rangeTermAnalysis whether or not terms should be analyzed for RangeQuerys
     */
    public void setRangeTermAnalysis(boolean rangeTermAnalysis);

    /**
     * Returns the configured value of the range-term-analysis functionality.
     * @return whether or not to analyze range terms when constructing {@link TermRangeQuery}s.
     */
    public boolean getRangeTermAnalysis();

    /**
     * Sets the maximum number of states that determinizing a regexp query can result in.
     * @param maxDeterminizedStates the maximum number of states to be set.
     */
    public void setMaxDeterminizedStates(int maxDeterminizedStates);

    /**
     * Returns the configured maximum number of states.
     * @return the maximum number of states that determinizing a regexp query can result in.
     */
    public int getMaxDeterminizedStates();
}
