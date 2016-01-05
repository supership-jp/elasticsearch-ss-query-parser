/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.spans.xst;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.lucene.search.spans.SpanQuery;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.ExternalQueryParser;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.InternalQueryParser;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.ProximityQueryDriver;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.xst.Node;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.xst.TreePath;

/**
 * This class represents each query fragments appears within raw query strings. In general, flatten form of proximity query
 * families could be undecidable while parsing, hence this class will be responsible for those ambigouity as well.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class Fragment implements Cloneable, Node<Fragment> {
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
	    if (isTermQuery) {
		this.isNearQuery(false);
		this.isOrQuery(false);
		this.isNotQuery(false);
	    }
	}

	// Returns true if the node represents SpanTermQuery.
	public boolean isNearQuery() {
	    return this.isNearQuery;
	}

	// Sets true if the node represents SpanNearQuery.
	public void isNearQuery(boolean isNearQuery) {
	    this.isNearQuery = isNearQuery;
	    if (isNearQuery) {
		this.isTermQuery(false);
		this.isOrQuery(false);
		this.isNotQuery(false);
	    }
	}

	// Returns true if the node represents SpanOrQuery.
	public boolean isOrQuery() {
	    return this.isOrQuery;
	}

	// Sets true if the node represents SpanOrQuery.
	public void isOrQuery(boolean isOrQuery) {
	    this.isOrQuery = isOrQuery;
	    if (isOrQuery) {
		this.isTermQuery(false);
		this.isNearQuery(false);
		this.isNotQuery(false);
	    }
	}

	// Returns true if the node represents SpanNotQuery.
	public boolean isNotQuery() {
	    return this.isNotQuery;
	}

	// Sets true if the node represents SpanNotQuery.
	public void isNotQuery(boolean isNotQuery) {
	    this.isNotQuery = isNotQuery;
	    if (isNotQuery) {
		this.isTermQuery(false);
		this.isNearQuery(false);
		this.isOrQuery(false);
	    }
	}
    }

    /** Holds the parent composition. */
    private Fragment parent;

    /** Holds currently handling queries. */
    private List<Fragment> children = new ArrayList<Fragment>();

    /** Holds currently handling queries. */
    private TreePath<Fragment> treePath;

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
    public Fragment(boolean infixed, int slop, int operator, boolean inOrder) {
	this.slop = slop;
	this.inOrder = inOrder;
	this.operator = operator;
	this.infixed = infixed;
    }

    /**
     * Constructor.
     */
    public Fragment(String field, String queryText, boolean infixed, int slop, int operator, boolean inOrder) {
	this(infixed, slop, operator, inOrder);
	this.field = field;
	this.queryText = queryText;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addChild(Fragment child) {
	this.children.add(child);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Fragment> getChildren() {
	return this.children;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setChildAt(int index, Fragment child) {
	this.children.add(index, child);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Fragment getChildAt(int index) {
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
    public int getIndexOf(Fragment child) {
	return this.children.indexOf(child);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParent(Fragment parent) {
	this.parent = parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Fragment getParent() {
	return this.parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTreePath(TreePath<Fragment> treePath) {
	this.treePath = treePath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TreePath<Fragment> getTreePath() {
	return this.treePath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLeaf() {
	return this.getChildCount() == 0;
    }

    /**
     * Transforms this archetype into the corresponding query.
     * @param field the field value to be handled.
     * @param tree the node belongs to.
     * @param driver the driver which is responsible to instanciate queries.
     */
    public SpanQuery toQuery(String field, ConcreteSyntaxTree tree, ProximityQueryDriver driver) {
	SpanQuery query = this.toConcrete(field, tree, driver);
	if (query != null && this.isWeighted()) {
	    query.setBoost(this.getWeight() * query.getBoost());
	}
	return query;
    }

    /**
     * Concretizes this archetype into the correspondig span query.
     * @param field the field value to be handled.
     * @param tree the node belongs to.
     * @param driver the driver which is responsible to instanciate queries.
     */
    protected SpanQuery toConcrete(String field, ConcreteSyntaxTree tree, ProximityQueryDriver driver) {
	Fragment.State state = tree.getStateOf(this.getTreePath());
	if (state != null) {
	    if (state.isNearQuery()) {
		return this.toSpanNearQuery(field, tree, driver);
	    } else if (state.isOrQuery()) {
		return this.toSpanOrQuery(field, tree, driver);
	    } else if (state.isNotQuery()) {
		return this.toSpanNotQuery(field, tree, driver);
	    } else {
		return this.toSpanTermQuery(field, tree, driver);
	    }
	} else {
	    return null;
	}
    }

    /**
     * Transforms this archetype into a span near query.
     * @param field the field value to be handled.
     * @param tree the node belongs to.
     * @param driver the driver which is responsible to instanciate queries.
     */
    protected SpanQuery toSpanTermQuery(String field, ConcreteSyntaxTree tree, ProximityQueryDriver driver) {
	if (this.getQueryText() != null && !(this.getQueryText().isEmpty())) {
	    return driver.getSpanTermQuery(field, this.getQueryText(), false);
	}
	return null;
    }

    /**
     * Transforms this archetype into a span near query.
     * @param field the field value to be handled.
     * @param tree the node belongs to.
     * @param driver the driver which is responsible to instanciate queries.
     */
    protected SpanQuery toSpanNearQuery(String field, ConcreteSyntaxTree tree, ProximityQueryDriver driver) {
	int i = 0;
	SpanQuery current;
	SpanQuery[] queries = new SpanQuery[this.getChildCount()];
	for (Fragment child : this.getChildren()) {
	    current = (SpanQuery) child.toQuery(field, tree, driver);
	    if (current != null) {
		current.setBoost(child.getWeight());
		queries[i++] = current;
	    }
	}
	if (i > 0) {
	    return driver.getSpanNearQuery(this.getSlop(), this.isInOrder(), Arrays.copyOfRange(queries, 0, i - 1));
	} else {
	    return null;
	}
    }

    /**
     * Transforms this archetype into a span or query.
     * @param field the field value to be handled.
     * @param tree the node belongs to.
     * @param driver the driver which is responsible to instanciate queries.
     */
    protected SpanQuery toSpanOrQuery(String field, ConcreteSyntaxTree tree, ProximityQueryDriver driver) {
	int i = 0;
	SpanQuery current;
	SpanQuery[] queries = new SpanQuery[this.getChildCount()];
	for (Fragment child : this.getChildren()) {
	    current = (SpanQuery) child.toQuery(field, tree, driver);
	    if (current != null) {
		current.setBoost(child.getWeight());
		queries[i++] = current;
	    }
	}
	if (i > 0) {
	    return driver.getSpanOrQuery(Arrays.copyOfRange(queries, 0, i - 1));
	} else {
	    return null;
	}
    }

    /**
     * Transforms this archetype into a span not query.
     * @param field the field value to be handled.
     * @param tree the node belongs to.
     * @param driver the driver which is responsible to instanciate queries.
     */
    protected SpanQuery toSpanNotQuery(String field, ConcreteSyntaxTree tree, ProximityQueryDriver driver) {
	int i = 0;
	int j = 0;
	SpanQuery current;
	SpanQuery[] inclusions = new SpanQuery[this.getChildCount()];
	SpanQuery[] exclusions = new SpanQuery[this.getChildCount()];
	for (Fragment child : this.getChildren()) {
	    current = (SpanQuery) child.toQuery(field, tree, driver);
	    if (current != null) {
		current.setBoost(child.getWeight());
		if (child.getOperator() == InternalQueryParser.MODIFIER_NEGATE) {
		    exclusions[j++] = current;
		} else {
		    inclusions[i++] = current;
		}
	    }
	}
	SpanQuery inclusion = null;
	SpanQuery exclusion = null;
	if (i > 0) {
	    inclusion = driver.getSpanOrQuery(Arrays.copyOfRange(inclusions, 0, i - 1));
	}
	if (j > 0) {
	    exclusion = driver.getSpanOrQuery(Arrays.copyOfRange(exclusions, 0, j - 1));
	}
	if (i > 0 && j > 0) {
	    return driver.getSpanNotQuery(inclusion, exclusion);
	} else {
	    return null;
	}
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
	for (Fragment child : this.children) {
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
	for (Fragment child : this.children) {
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
    public Fragment clone() {
	try {
	    return (Fragment) super.clone();
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
