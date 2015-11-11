/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.classic.intermediate;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.queryparser.flexible.standard.CommonQueryParserConfiguration;

/**
 * This interface specifies the implementing class can be configured in accordance with the
 * Lucene's {@code CommonQueryParserConfiguration} API and some additional context.
 *
 * @author Shingo OKAWA
 * @since  08/11/2015
 */
public interface QueryParserContext extends CommonQueryParserConfiguration {
    /**
     * Holds pre-defined operators.
     */
    public static enum Operator {
        NONE("", 0),
        AND("AND", 1),
        OR("OR", 1);

        // Holds actual string expression.
        private String expression;
        // Holds actual string expression.
        private int precedence;

        // Constructor.
        private Operator(String expression, int precedence) {
            this.expression = expression;
            this.precedence = precedence;
        }

        // Returns corresponding enum instance from the given expression.
        public static Operator find(String expression) {
            for (Operator operator : Operator.values()) {
                if (expression.equals(operator.expression)) {
                    return operator;
                }
            }
            return Operator.NONE;
        }

        /** @inheritDoc */
        @Override
        public String toString() {
            return this.expression;
        }
    }

    /**
     * Holds pre-defined modifiers.
     */
    public static enum Modifier {
        NONE("", 0),
        NOT("-", 1),
        REQUIRED("_", 1);

        // Holds actual string expression.
        private String expression;
        // Holds actual string expression.
        private int precedence;

        // Constructor.
        private Modifier(String expression, int precedence) {
            this.expression = expression;
            this.precedence = precedence;
        }

        // Returns corresponding enum instance from the given expression.
        public static Modifier find(String expression) {
            for (Modifier modifier : Modifier.values()) {
                if (expression.equals(modifier.expression)) {
                    return modifier;
                }
            }
            return Modifier.NONE;
        }

        /** @inheritDoc */
        @Override
        public String toString() {
            return this.expression;
        }
    }

    /**
     * Holds pre-defined conjuinctions.
     */
    public static enum Conjunction {
        NONE("", 0),
        AND("AND", 1),
        OR("OR", 1);

        // Holds actual string expression.
        private String expression;
        // Holds actual string expression.
        private int precedence;

        // Constructor.
        private Conjunction(String expression, int precedence) {
            this.expression = expression;
            this.precedence = precedence;
        }

        // Returns corresponding enum instance from the given expression.
        public static Conjunction find(String expression) {
            for (Conjunction conjunction : Conjunction.values()) {
                if (expression.equals(conjunction.expression)) {
                    return conjunction;
                }
            }
            return Conjunction.NONE;
        }

        /** @inheritDoc */
        @Override
        public String toString() {
            return this.expression;
        }
    }

    /**
     * Holds pre-defined wildcards.
     */
    public static enum Wildcard {
        NONE("", 0),
        ANY_STRING("*", 1),
        ANY_CHARACTER("?", 1);

        // Holds actual string expression.
        private String expression;
        // Holds actual string expression.
        private int precedence;

        // Constructor.
        private Wildcard(String expression, int precedence) {
            this.expression = expression;
            this.precedence = precedence;
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

        /** @inheritDoc */
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
    public void setDefaultOperator(Operator defaultOperator);

    /**
     * Returns the default boolean operator, which will be either <code>Operators.AND</code> or <code>Operators.OR</code>.
     * @return the assigned default boolean operator.
     */
    public Operator getDefaultOperator();

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
