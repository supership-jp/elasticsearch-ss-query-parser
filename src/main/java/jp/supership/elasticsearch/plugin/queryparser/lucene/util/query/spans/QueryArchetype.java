/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.spans;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.ProximityQueryDriver;

/**
 * This class is responsible for base implementation of the argumented query classes.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public abstract class QueryArchetype implements Cloneable {
    /** Holds the empty query. */
    public final static Query THE_EMPTY_QUERY = new BooleanQuery() {
	    /** {@inheritDoc} */
	    @Override
	    public void setBoost(float boost) {
		throw new UnsupportedOperationException();
	    }

	    /** {@inheritDoc} */
	    @Override
	    public void add(BooleanClause clause) {
		throw new UnsupportedOperationException();
	    }

	    /** {@inheritDoc} */
	    @Override
	    public void add(Query query, BooleanClause.Occur occur) {
		throw new UnsupportedOperationException();
	    }
	};

    /** Holds open weight operator. */
    public final static String WEIGHT_OPERATOR = "^";

    /** Holds weight value. */
    private float weight = 1.0f;

    /** Sets to be true if the weight value is specified. */
    private boolean weighted = false;

    /**
     * Constructor.
     */
    public QueryArchetype() {
	// DO NOTHING.
    }

    /**
     * Returns {@code Query} in accordance to the assigned {@code ProximityQueryDriver} withou boosting.
     */
    public abstract Query generate(String field, ProximityQueryDriver driver);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract String toString();

    /**
     * Sets true if the weight value is assigned.
     * @param weighted the configuration value to be set.
     */
    public void isWeighted(boolean weighted) {
	this.weighted = weighted;
    }

    /**
     * Returns true if the weight value is assigned.
     * @return true if the weight value is assigned.
     */
    public boolean isWeighted() {
	return this.weighted;
    }

    /**
     * Sets the weight value.
     * @param weight the weight value to be set.
     */
    public void setWeight(float weight) {
	this.weight = weight;
    }

    /**
     * Returns the assigned weight value.
     * @return the assigned weight value.
     */
    public float getWeight() {
	return this.weight;
    }

    /**
     * Appends the weight part of query.
     * @param builder the currently handling builder.
     */
    protected void weightToString(StringBuilder builder) {
	if (this.isWeighted()) {
	    builder.append(WEIGHT_OPERATOR);
	    builder.append(String.valueOf(this.getWeight()));
	}
    }

    /**
     * Returns {@code Query} in accordance to the assigned {@code ProximityQueryDriver}.
     * @param  field the currently handling field.
     * @param  driver the driver which is responsible to instanciate queries.
     * @return new {@link Query} instance.
     */
    public Query toQuery(String field, ProximityQueryDriver driver) {
	Query query = this.generate(field, driver);
	if (this.isWeighted()) {
	    query.setBoost(this.getWeight() * query.getBoost());
	}
	return query;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryArchetype clone() {
	try {
	    return (QueryArchetype) super.clone();
	} catch (CloneNotSupportedException cause) {
	    throw new Error(cause);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
	return this.getClass().hashCode() ^ this.toString().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object that) {
	if (that == null) {
	    return false;
	} else if (!this.getClass().equals(that.getClass())) {
	    return false;
	} else {
	    return this.toString().equals(that.toString());
	}
    }
}
