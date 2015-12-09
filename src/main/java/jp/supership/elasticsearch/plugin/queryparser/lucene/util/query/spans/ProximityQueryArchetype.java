/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.spans;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanQuery;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.ProximityQueryDriver;

/**
 * This class represents queries which is able to handle proximity search.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public abstract class ProximityQueryArchetype extends QueryComposition {
    /** Holds slop value. */
    private int slop;

    /** Sets to be true if the queries must be ordered. */
    private boolean inOrder;

    /** Sets to be true if the queries must be ordered. */
    private Map<SpanQuery, Float> spanQueries = new HashMap<SpanQuery, Float>();

    /**
     * Constructor.
     */
    public ProximityQueryArchetype(boolean infixed, int slop, int operator, boolean inOrder) {
	super(infixed, operator);
	this.slop = slop;
	this.inOrder = inOrder;
    }

    /**
     * Constructor.
     */
    public ProximityQueryArchetype(ProximityQueryArchetype parent, boolean infixed, int slop, int operator, boolean inOrder) {
	super(parent, infixed, operator);
	this.slop = slop;
	this.inOrder = inOrder;
    }

    /**
     * Adds given query into the internal context.
     * @param query the query to be added.
     */
    public void add(Query query) {
	if (query == QueryArchetype.THE_EMPTY_QUERY) {
	    return;
	} else if (!(query instanceof SpanQuery)) {
	    throw new AssertionError("expected span query: " + query.toString());
	}
	this.add((SpanQuery) query, query.getBoost());
    }

    /**
     * Adds given query into the internal context.
     * @param query the query to be added.
     * @param weight the weight valeu which is relating to the given query.
     */
    public void add(SpanQuery query, float weight) {
	Float value = this.spanQueries.get(query);
	if (value != null) {
	    value = Float.valueOf(value.floatValue() + weight);
	} else {
	    value = Float.valueOf(weight);
	}
	this.spanQueries.put(query, value); 
    }

    /**
     * Adds given query into the internal context.
     * @param term the tern to be added.
     * @param weight the weight valeu which is relating to the given term.
     */
    public void add(Term term, float weight) {
	// TODO: implement this method.
    }

    /**
     * Sets the query ordering configuration.
     * @param inOrder the configuration value to be set.
     */
    public void isInOrder(boolean inOrder) {
	this.inOrder = inOrder;
    }

    /**
     * Returns true if the query ordering configuration is set to be true.
     * @return true if the query ordering configuration is set to be set.
     */
    public boolean isInOrder() {
	return this.inOrder;
    }

    /**
     * Sets the slop value
     * @param slop the slop width to be set.
     */
    public void setSlop(int slop) {
	this.slop = slop;
    }

    /**
     * Returns the assigned slop value.
     * @return the assigned slop value.
     */
    public int getSlop() {
	return this.slop;
    }
}
