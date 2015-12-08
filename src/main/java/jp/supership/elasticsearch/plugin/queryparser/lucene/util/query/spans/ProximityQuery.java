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
public abstract class ProximityQuery extends QueryComposition implements ProximitySubQueries {
    /** Holds slop value. */
    private int slop;

    /** Sets to be true if the queries must be ordered. */
    private boolean inOrder;

    /** Sets to be true if the queries must be ordered. */
    private Map<SpanQuery, Float> spanQueries = new HashMap<SpanQuery, Float>();

    /**
     * Constructor.
     */
    public ProximityQuery(Collection<ArgumentedQuery> queries, boolean infixed, int slop, int operator, boolean inOrder) {
	super(queries, infixed, operator);
	this.slop = slop;
	this.inOrder = inOrder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
	return this.spanQueries.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
	return this.spanQueries.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(Object key) {
	return this.spanQueries.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsValue(Object value) {
	return this.spanQueries.containsValue(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Float get(Object key) {
	return this.spanQueries.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Float put(SpanQuery key, Float value) {
	return this.spanQueries.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Float remove(Object key) {
	return this.spanQueries.remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAll(Map<? extends SpanQuery, ? extends Float> map) {
	this.spanQueries.putAll(map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
	this.spanQueries.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<SpanQuery> keySet() {
	return this.spanQueries.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Float> values() {
	return this.spanQueries.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Map.Entry<SpanQuery, Float>> entrySet() {
	return this.spanQueries.entrySet();
    }

    /**
     * Adds given query into the internal context.
     * @param query the query to be added.
     */
    public void add(Query query) {
	if (query == ArgumentedQuery.THE_EMPTY_QUERY) {
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
	Float value = this.get(query);
	if (value != null) {
	    value = Float.valueOf(value.floatValue() + weight);
	} else {
	    value = Float.valueOf(weight);
	}
	this.put(query, value); 
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
