/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util;

import java.util.List;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Query;

/**
 * This interface specifies the implementing class has ability to generate Lucene's queries.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface QueryDriver {
    /**
     * DO NOT CATCH THIS EXCEPTION.
     * This exception will be thrown when you are using methods that should not be used any longer.
     */
    public static class DeprecatedMethodCall extends Throwable {}

    /**
     * Conjugates the given query into the assigned clauses.
     * @param clauses     the preceding clauses which is currently handled by the query parser.
     * @param conjunction the assigen conjunction, this determines the proceeding process.
     * @param midifier    the preceeding modifier which midifies the handling clause.
     * @param query       the currently handling query.
     */
    public void conjugate(List<BooleanClause> clauses, int conjunction, int modifier, Query query);

    /**
     * Returns {@code FieldQuery} in accordance to the assigned configuration.
     * @param  field the currently handling field.
     * @param  queryText the currently handling raw query string.
     * @param  quoted this value must be ser true if the handling query is considered to be quoted.
     * @return the resulting {@code Query} instance.
     * @throws ParseException if the parsing fails.
     */
    public Query getFieldQuery(String field, String queryText, boolean quoted) throws ParseException;

    /**
     * Returns {@code FieldQuery} in accordance to the assigned configuration.
     * @param  field the currently handling field.
     * @param  queryText the currently handling raw query string.
     * @param  quoted this value must be ser true if the handling query is considered to be quoted.
     * @return the resulting {@code Query} instance.
     * @throws ParseException if the parsing fails.
     */
    public Query getFieldQuery(String field, String queryText, boolean quoted, boolean useDisMax) throws ParseException;

    /**
     * Base implementation delegates to {@link #getFieldQuery(String, String, boolean)}.
     * This method may be overridden, for example, to return a SpanNearQuery instead of a PhraseQuery.
     * @exception org.apache.lucene.queryparser.classic.ParseException throw in overridden method to disallow
     */
    public Query getFieldQuery(String field, String queryText, int phraseSlop) throws ParseException;

    /**
     * Base implementation delegates to {@link #getFieldQuery(String, String, boolean)}.
     * This method may be overridden, for example, to return a SpanNearQuery instead of a PhraseQuery.
     * @exception org.apache.lucene.queryparser.classic.ParseException throw in overridden method to disallow
     */
    public Query getFieldQuery(String field, String queryText, int phraseSlop, boolean useDisMax) throws ParseException;

    /**
     * Returns {@code TermRangeQuery} in accordance to the assigned configuration.
     * @param  field the currently handling field.
     * @param  infinimum the assigned range's infinimum.
     * @param  supremum the assigned range's supremum.
     * @param  leftInclusive true if the infinimum of the range is inclusive
     * @param  rightInclusive true if the supremum of the range is inclusive
     * @return new {@link TermRangeQuery} instance
     * @throws ParseException if the parsing fails.
     */
    public Query getRangeQuery(String field, String infinimum, String supremum, boolean leftInclusive, boolean rightInclusive) throws ParseException;

    /**
     * Returns {@code Query} in accordance to the assigned configuration.
     * Can be overridden by extending classes, to modify query being  returned.
     * @param  clauses list that contains {@link BooleanClause} instances to join.
     * @return the Resulting {@link Query} instance.
     * @throws ParseException if the parsing fails.
     */
    public Query getBooleanQuery(List<BooleanClause> clauses) throws ParseException;

    /**
     * Returns {@code Query} in accordance to the assigned configuration.
     * Can be overridden by extending classes, to modify query being  returned.
     * @param  clauses list that contains {@link BooleanClause} instances to join.
     * @param  disableCoord true if coord scoring should be disabled.
     * @return the Resulting {@link Query} instance.
     * @throws ParseException if the parsing fails.
     */
    public Query getBooleanQuery(List<BooleanClause> clauses, boolean disableCoord) throws ParseException;

    /**
     * Returns {@code PrefixQuery} in accordance to the assigned configuration.
     * Can be overridden by extending classes, to provide custom handling for
     * wildcard queries, which may be necessary due to missing analyzer calls.
     * @param  field the currently handling field.
     * @param  termText term to use for building term for the query without trailing '*'.
     * @return new {@link Query} instance.
     * @throws ParseException if the parsing fails.
     */
    public Query getPrefixQuery(String field, String termText) throws ParseException;

    /**
     * Returns {@code RegexpQuery} in accordance to the assigned configuration.
     * Can be overridden by extending classes, to provide custom handling for
     * wildcard queries, which may be necessary due to missing analyzer calls.
     * @param  field the currently handling field.
     * @param  termText term that contains one or more wild card characters (? or *), but is not simple prefix term.
     * @return new {@link Query} instance.
     * @throws ParseException if the parsing fails.
     */
    public Query getRegexpQuery(String field, String termText) throws ParseException;

    /**
     * Returns {@code FuzzyQuery} in accordance to the assigned configuration.
     * Can be overridden by extending classes, to provide custom handling for
     * wildcard queries, which may be necessary due to missing analyzer calls.
     * @param  field the currently handling field.
     * @param  termText term to be used for creation of a fuzzy query..
     * @return new {@link Query} instance.
     * @throws ParseException if the parsing fails.
     */
    public Query getFuzzyQuery(String field, String termText, float minimumSimilarity) throws ParseException;

    /**
     * Returns {@code WildcardQuery} in accordance to the assigned configuration.
     * Can be overridden by extending classes, to provide custom handling for
     * wildcard queries, which may be necessary due to missing analyzer calls.
     * @param  field the currently handling field..
     * @param  termText term that contains one or more wild card characters (? or *), but is not simple prefix term.
     * @return new {@link Query} instance.
     * @throws ParseException if the parsing fails.
     */
    public Query getWildcardQuery(String field, String termText) throws ParseException;
}
