/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util;

import java.util.List;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.spans.SpanQuery;

/**
 * This interface specifies the implementing class has ability to generate Lucene's span queries.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface ProximityQueryDriver extends QueryDriver {
    /**
     * Conjugates the given query into the assigned clauses.
     * @param clauses     the preceding clauses which is currently handled by the query parser.
     * @param conjunction the assigen conjunction, this determines the proceeding process.
     * @param modifier    the preceeding modifier which midifies the handling clause.
     * @param slop        the assigned slop value.
     * @param inOrder     the assigned ordering value.
     * @param query       the currently handling query.
     */
    public void conjugate(List<SpanQuery> clauses, int conjunction, int modifier, int slop, boolean inOrder, SpanQuery query);

    /**
     * Returns {@code SpanTermQuery} in accordance to the assigned configuration.
     * @param  field the currently handling field.
     * @param  queryText the currently handling raw query string.
     * @param  quoted this value must be ser true if the handling query is considered to be quoted.
     * @return the resulting {@code SpanQuery} instance.
     * @throws ParseException if the parsing fails.
     */
    public SpanQuery getSpanTermQuery(String field, String queryText, boolean quoted) throws ParseException;

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
