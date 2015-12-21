/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.spans.archetype;

import java.util.ArrayList;
import java.util.List;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.ast.Node;

/**
 * This class represents ambiguous queries, i.e., unspecified queries within parsing, which is able to handle proximity search.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class ProximityArchetype implements Cloneable, Node<ProximityArchetype> {
    /** Holds open parentheis. */
    public final static String OPEN_PARENTHESIS = "(";

    /** Holds close parentheis. */
    public final static String CLOSE_PARENTHESIS = ")";

    /** Holds whitespace. */
    public final static String WHITESPACE = " ";

    /** Holds separator. */
    public final static String SEPARATOR = ",";

    /** Holds weight operator. */
    public final static String WEIGHT_OPERATOR = "^";

    public static class State extends Node.State {
	// Holds true if the node represents SpanTermQuery.
	private boolean isTermQuery = true;
	// Holds true if the node represents SpanNearQuery.
	private boolean isNearQuery = false;
	// Holds true if the node represents SpanOrQuery.
	private boolean isOrQuery = false;
	// Holds true if the node represents SpanNotQuery.
	private boolean isNotQuery = false;

	// Constructor.
	public State() {
	    super();
	}

	// Returns true if the node represents SpanTermQuery.
	public boolean isTermQuery() {
	    return this.isTermQuery;
	}

	// Sets true if the node represents SpanTermQuery.
	public void isTermQuery(boolean isTermQuery) {
	    this.isTermQuery = isTermQuery;
	}

	// Returns true if the node represents SpanTermQuery.
	public boolean isNearQuery() {
	    return this.isNearQuery;
	}

	// Sets true if the node represents SpanNearQuery.
	public void isNearQuery(boolean isNearQuery) {
	    this.isNearQuery = isNearQuery;
	}

	// Returns true if the node represents SpanOrQuery.
	public boolean isOrQuery() {
	    return this.isOrQuery;
	}

	// Sets true if the node represents SpanOrQuery.
	public void isOrQuery(boolean isOrQuery) {
	    this.isOrQuery = isOrQuery;
	}

	// Returns true if the node represents SpanNotQuery.
	public boolean isNotQuery() {
	    return this.isNotQuery;
	}

	// Sets true if the node represents SpanNotQuery.
	public void isNotQuery(boolean isNotQuery) {
	    this.isNotQuery = isNotQuery;
	}
    }

    /** Holds the parent composition. */
    private ProximityArchetype parent;

    /** Holds currently handling queries. */
    private List<ProximityArchetype> children = new ArrayList<ProximityArchetype>();

    /** Holds field value. */
    private String field;

    /** Holds query text value. */
    private String queryText;

    /** Holds weight value. */
    private float weight = 1.0f;

    /** Sets to be true if the weight value is specified. */
    private boolean weighted = false;

    /** Sets to be true if the operator is infixed. */
    private boolean infixed;

    /** Holds slop value. */
    private int slop;

    /** Holds operator number. */
    private int operator;

    /** Sets to be true if the queries must be ordered. */
    private boolean inOrder;

    /**
     * Constructor.
     */
    public ProximityArchetype(boolean infixed, int slop, int operator, boolean inOrder) {
	this.slop = slop;
	this.inOrder = inOrder;
	this.operator = operator;
	this.infixed = infixed;
    }

    /**
     * Constructor.
     */
    public ProximityArchetype(String field, String queryText, boolean infixed, int slop, int operator, boolean inOrder) {
	this(infixed, slop, operator, inOrder);
	this.field = field;
	this.queryText = queryText;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addChild(ProximityArchetype child) {
	this.children.add(child);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ProximityArchetype> getChildren() {
	return this.children;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setChildAt(int index, ProximityArchetype child) {
	this.children.add(index, child);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProximityArchetype getChildAt(int index) {
	return this.children.get(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getChildCount() {
	return this.children.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIndexOf(ProximityArchetype child) {
	return this.children.indexOf(child);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParent(ProximityArchetype parent) {
	this.parent = parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProximityArchetype getParent() {
	return this.parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLeaf() {
	return this.getChildCount() == 0;
    }

    /**
     * Sets the field value.
     * @param field the field value to be set.
     */
    public void setField(String field) {
	this.field = field;
    }

    /**
     * Returns the assigned field value.
     * @return the assigned field value.
     */
    public String getField() {
	return this.field;
    }

    /**
     * Sets the query text value.
     * @param queryText the query text value to be set.
     */
    public void setQueryText(String queryText) {
	this.queryText = queryText;
    }

    /**
     * Returns the assigned query text value.
     * @return the assigned query text value.
     */
    public String getQueryText() {
	return this.queryText;
    }

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
	this.weighted = true;
    }

    /**
     * Returns the assigned weight value.
     * @return the assigned weight value.
     */
    public float getWeight() {
	return this.weight;
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
     * Stringifies the instance in accordance to the infixed context.
     * @param builder the currently handling builder.
     */
    protected void infixToString(StringBuilder builder) {
	builder.append(OPEN_PARENTHESIS);
	for (ProximityArchetype child : this.children) {
	    builder.append(WHITESPACE);
	    builder.append(String.valueOf(this.getOperator()));
	    builder.append(WHITESPACE);
	    builder.append(child.toString());
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
	for (ProximityArchetype child : this.children) {
	    builder.append(prefix);
	    prefix = SEPARATOR;
	    builder.append(child.toString());
	}
	builder.append(CLOSE_PARENTHESIS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProximityArchetype clone() {
	try {
	    return (ProximityArchetype) super.clone();
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
