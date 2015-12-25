/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util;

import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanQuery;

/**
 * This interface specifies the implementing class has ability to generate Lucene's span queries.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface ProximityQueryDriver {
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
     * Returns a span query from the analysis chain.
     * @param analyzer analyzer used for this query.
     * @param operator the default boolean operator used for this query.
     * @param field field to create queries against.
     * @param queryText text to be passed to the analysis chain.
     * @param quoted true if phrases should be generated when terms occur at more than one position.
     * @param phraseSlop slop factor for span and/or phrase/multiphrase queries.
     * @param inOrder true if the order is important.
     */
    public Query getFieldQuery(Analyzer analyzer, String field, String queryText, boolean quoted, int phraseSlop, boolean inOrder, boolean useDisMax) throws ParseException;

    /**
     * Returns {@code Query} in accordance to the assigned configuration.
     * Can be overridden by extending classes, to modify query being  returned.
     * @param  disableCoord true if coord scoring should be disabled.
     * @return the Resulting {@link Query} instance.
     * @throws ParseException if the parsing fails.
     */
    public Query getBooleanQuery(boolean disableCoord) throws ParseException;

    /**
     * Returns {@code Query} in accordance to the assigned configuration.
     * Can be overridden by extending classes, to modify query being  returned.
     * @param  clauses list that contains {@link BooleanClause} instances to join.
     * @return the Resulting {@link Query} instance.
     * @throws ParseException if the parsing fails.
     */
    public Query getBooleanQuery(List<BooleanClause> clauses) throws ParseException;

    /**
     * Returns {@code SpanTermQuery} in accordance to the assigned configuration.
     * @param  field the currently handling field.
     * @param  termText the currently handling raw term string.
     * @param  quoted this value must be ser true if the handling query is considered to be quoted.
     * @return the resulting {@code SpanQuery} instance.
     * @throws ParseException if the parsing fails.
     */
    public SpanQuery getSpanTermQuery(String field, String termText, boolean quoted) throws ParseException;

    /**
     * Returns {@code SpanNotQuery} in accordance to the assigned configuration.
     * @param  inclusion the span clause to be filtered in.
     * @param  exclusion the span clause to be filtered out.
     * @return the resulting {@code SpanQuery} instance.
     * @throws ParseException if the parsing fails.
     */
    public SpanQuery getSpanNotQuery(SpanQuery inclusion, SpanQuery exclusion) throws ParseException;

    /**
     * Returns {@code SpanNearQuery} in accordance to the assigned configuration.
     * @param  slop    the assigned slop value.
     * @param  inOrder the assigned ordering value.
     * @param  clauses the span clause which constructs this query.
     * @return the resulting {@code SpanQuery} instance.
     * @throws ParseException if the parsing fails.
     */
    public SpanQuery getSpanNearQuery(int slop, boolean inOrder, SpanQuery... clauses) throws ParseException;

    /**
     * Returns {@code SpanNearQuery} in accordance to the assigned configuration.
     * @param  clauses the span clause which constructs this query.
     * @return the resulting {@code SpanQuery} instance.
     * @throws ParseException if the parsing fails.
     */
    public SpanQuery getSpanOrQuery(SpanQuery... clauses) throws ParseException;

    /**
     * Returns {@code SpanMultiTermQueryWrapper} in accordance to the assigned configuration.
     * @param  field the currently handling field.
     * @return the resulting {@code SpanQuery} instance.
     * @throws ParseException if the parsing fails.
     */
    public <Q extends MultiTermQuery> SpanQuery getSpanMultiTermQueryWrapper(Q wrapped) throws ParseException;
}
