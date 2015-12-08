/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.spans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.lucene.search.Query;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.ProximityQueryDriver;

/**
 * This class is responsible for base implementation of the argumented composited queries.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public abstract class QueryComposition extends ArgumentedQuery {
    /** Holds open parentheis. */
    public final static String OPEN_PARENTHESIS = "(";

    /** Holds close parentheis. */
    public final static String CLOSE_PARENTHESIS = ")";

    /** Holds whitespace. */
    public final static String WHITESPACE = " ";

    /** Holds separator. */
    public final static String SEPARATOR = ",";

    /**
     * Represents span query's constructing context.
     */
    protected class Queries extends ArrayList<ArgumentedQuery> {
	/** Adds new span query to this metadata. */
	public void add(Collection<ArgumentedQuery> queries) {
	    for (ArgumentedQuery query : queries) {
		this.add(query);
	    }
	}
    }

    /** Holds operator number. */
    protected int operator;

    /** Sets to be true if the operator is infixed. */
    protected boolean infixed;

    /** Holds currently handling queries. */
    protected Queries argumentedQueries = new Queries();

    /**
     * Constructor.
     */
    public QueryComposition(Collection<ArgumentedQuery> queries, boolean infixed, int operator) {
	this.setArgumentedQueries(queries);
	this.infixed = infixed;
	this.operator = operator;
    }

    /**
     * Sets the handling queries.
     * @param queries the queries to be set.
     */
    public void setArgumentedQueries(Collection<ArgumentedQuery> queries) {
	if (queries.size() < 2) {
	    throw new AssertionError("too few subqueries");
	}
	this.argumentedQueries.add(queries);
    }

    /**
     * Return the currently handling queries.
     * @return the currently handling queries.
     */
    public Collection<ArgumentedQuery> getArgumentedQueries() {
	return this.argumentedQueries;
    }

    /**
     * Returns the assigned operator number.
     * @return the assigned operator number.
     */
    public int getOperator() {
	return this.operator;
    }

    /**
     * Sets the operator.
     * @operator operator the operator number to be set.
     */
    public void setOperator(int operator) {
	this.operator = operator;
    }

    /**
     * Setss true if the operator is infixed.
     * @return infixed the configuration value to be set.
     */
    public void isInfixed(boolean infixed) {
	this.infixed = infixed;
    }

    /**
     * Returns true if the operator is infixed.
     * @return true if the operator is ingfixed.
     */
    public boolean isInfixed() {
	return this.infixed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	if (this.isInfixed()) {
	    this.infixToString(builder);
	} else {
	    this.prefixToString(builder);
	}
	this.weightToString(builder);
	return builder.toString();
    }

    /**
     * Stringifies the instance in accordance to the infixed context.
     * @param builder the currently handling builder.
     */
    protected void infixToString(StringBuilder builder) {
	builder.append(OPEN_PARENTHESIS);
	for (ArgumentedQuery query : this.argumentedQueries) {
	    builder.append(WHITESPACE);
	    builder.append(String.valueOf(this.getOperator()));
	    builder.append(WHITESPACE);
	    builder.append(query.toString());
	}
	builder.append(CLOSE_PARENTHESIS);
    }

    /**
     * Stringifies the instance in accordance to the prefixed context.
     * @param builder the currently handling builder.
     */
    protected void prefixToString(StringBuilder builder) {
	builder.append(String.valueOf(this.getOperator()));
	builder.append(OPEN_PARENTHESIS);
	String prefix = "";
	for (ArgumentedQuery query : this.argumentedQueries) {
	    builder.append(prefix);
	    prefix = SEPARATOR;
	    builder.append(query.toString());
	}
	builder.append(CLOSE_PARENTHESIS);
    }
}
