/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.spans;

import java.util.Collection;
import java.util.Map;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanQuery;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.ExternalQueryParser;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.ProximityQueryDriver;

/**
 * This class represents span family NEAR query.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class SpanNotQueryArchetype extends ProximityQueryArchetype {
    /** Holds field name. */
    private String field;

    /**
     * Constructor.
     */
    public SpanNotQueryArchetype(boolean infixed, int slop, int operator, boolean inOrder) {
	super(infixed, slop, operator, inOrder);
    }

    /**
     * Constructor.
     */
    public SpanNotQueryArchetype(ProximityQueryArchetype parent, boolean infixed, int slop, int operator, boolean inOrder) {
	super(parent, infixed, slop, operator, inOrder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query generate(String field, ProximityQueryDriver driver) {
	int i = 0;
	int j = 0;
	SpanQuery current;
	SpanQuery[] inclusion = new SpanQuery[this.getChildCount()];
	SpanQuery[] exclusion = new SpanQuery[this.getChildCount()];
	for (QueryComposition composition : this.getChildren()) {
	    current = (SpanQuery) composition.toQuery(field, driver);
	    current.setBoost(composition.getWeight());
	    if (composition.getOperator() == ExternalQueryParser.MODIFIER_NEGATE) {
		exclusion[i++] = current;
	    } else {
		inclusion[j++] = current;
	    }
	}
	return driver.getSpanNotQuery(inclusion, exclusion);
    }
}
