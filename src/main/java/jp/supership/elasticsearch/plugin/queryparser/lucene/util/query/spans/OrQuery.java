/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.spans;

import java.util.Collection;
import java.util.Map;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanQuery;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.ProximityQueryDriver;

/**
 * This class represents span family OR query.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class OrQuery extends ProximityQuery {
    /**
     * Constructor.
     */
    public OrQuery(Collection<ArgumentedQuery> queries, boolean infixed, int slop, int operator, boolean inOrder) {
	super(queries, infixed, slop, operator, inOrder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query generate(String field, ProximityQueryDriver driver) {
	int i = 0;
	SpanQuery current;
	SpanQuery[] queries = new SpanQuery[this.size()];
	for (Map.Entry<SpanQuery, Float> entry : this.entrySet()) {
	    current = entry.getKey();
	    current.setBoost(entry.getValue());
	    queries[i++] = current;
	}
	if (queries.length == 1) {
	    return queries[0];
	} else {
	    return driver.getSpanOrQuery(queries);
	}
    }
}
