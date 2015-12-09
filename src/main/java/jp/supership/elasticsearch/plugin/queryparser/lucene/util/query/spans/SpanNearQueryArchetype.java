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
 * This class represents span family NEAR query.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class SpanNearQueryArchetype extends ProximityQueryArchetype {
    /** Holds field name. */
    private String field;

    /**
     * Constructor.
     */
    public SpanNearQueryArchetype(boolean infixed, int slop, int operator, boolean inOrder) {
	super(infixed, slop, operator, inOrder);
    }

    /**
     * Constructor.
     */
    public SpanNearQueryArchetype(ProximityQueryArchetype parent, boolean infixed, int slop, int operator, boolean inOrder) {
	super(parent, infixed, slop, operator, inOrder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query generate(String field, ProximityQueryDriver driver) {
	int i = 0;
	SpanQuery current;
	SpanQuery[] queries = new SpanQuery[this.getChildCount()];
	for (QueryComposition archetype : this.getChildren()) {
	    current = (SpanQuery) archetype.toQuery(field, driver);
	    current.setBoost(archetype.getWeight());
	    queries[i++] = current;
	}
	if (queries.length == 1) {
	    return queries[0];
	} else {
	    return driver.getSpanNearQuery(this.getSlop(), this.isInOrder(), queries);
	}
    }
}
