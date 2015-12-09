/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.spans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.lucene.search.Query;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.ProximityQueryDriver;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.AST;

/**
 * This class is responsible for base implementation of the argumented composited queries.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public abstract class QueryComposition extends QueryArchetype implements AST<QueryComposition> {
    /** Holds open parentheis. */
    public final static String OPEN_PARENTHESIS = "(";

    /** Holds close parentheis. */
    public final static String CLOSE_PARENTHESIS = ")";

    /** Holds whitespace. */
    public final static String WHITESPACE = " ";

    /** Holds separator. */
    public final static String SEPARATOR = ",";

    /** Holds operator number. */
    protected int operator;

    /** Sets to be true if the operator is infixed. */
    protected boolean infixed;

    /** Holds the parent composition. */
    protected QueryComposition parent;

    /** Holds currently handling queries. */
    protected List<QueryComposition> children = new ArrayList<QueryComposition>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void addChild(QueryComposition child) {
	this.children.add(child);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<QueryComposition> getChildren() {
	return this.children;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryComposition getChildAt(int index) {
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
    public int getIndexOf(QueryComposition child) {
	return this.children.indexOf(child);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParent(QueryComposition parent) {
	this.parent = parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryComposition getParent() {
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
     * Constructor.
     */
    public QueryComposition(boolean infixed, int operator) {
	this.setParent(null);
	this.infixed = infixed;
	this.operator = operator;
    }

    /**
     * Constructor.
     */
    public QueryComposition(QueryComposition parent, boolean infixed, int operator) {
	this.setParent(parent);
	this.infixed = infixed;
	this.operator = operator;
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
	for (QueryArchetype child : this.children) {
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
	for (QueryArchetype child : this.children) {
	    builder.append(prefix);
	    prefix = SEPARATOR;
	    builder.append(child.toString());
	}
	builder.append(CLOSE_PARENTHESIS);
    }
}
