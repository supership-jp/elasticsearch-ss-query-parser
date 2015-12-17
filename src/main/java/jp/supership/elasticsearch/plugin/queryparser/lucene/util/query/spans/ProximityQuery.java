/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.spans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spans.SpanQuery;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.AST;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.ASTVisitor;

/**
 * This class is responsible for base implementation of the argumented composited queries.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public abstract class ProximityQuery extends SpanQuery implements Node<ProximityQuery> {
    /** Holds field name. */
    private String field;

    /** Holds slop width. */
    private int slop;

    /** Holds true if the order is important. */
    private boolean inOrder;

    /** Holds the parent composition. */
    private ProximityQuery parent;

    /** Holds currently handling queries. */
    private List<ProximityQuery> children = new ArrayList<ProximityQuery>();

    /** Represents rewrite method context. */
    private class RewriteContext extends ASTVisitor<ProximityQuery>.Context {
	/** Holds index reader. */
	private IndexReader reader;

	/** Holds index reader. */
	private ProximityQuery tree;

	/** Constructor. */
	public RewriteContext(IndexReader reader, ProximityQuery tree) {
	    this.reader = reader;
	    this.root = root;
	}

	/** Sets the index reader. */
	public void setIndexReader(IndexReader reader) {
	    this.reader = reader;
	}

	/** Returns the index reader. */
	public IndexReader getIndexReader() {
	    return this.reader;
	}

	/** Sets the root node. */
	public void setRoot(ProximityQuery root) {
	    this.root = root;
	}

	/** Returns the root node. */
	public ProximityQuery getRoot() {
	    return this.root;
	}
    }

    /** Represents rewrite method callback functions. */
    private class RewriteCallback implements ASTVisitor<ProximityQuery>.Callback<RewriteContext> {
	/** Constructor. */
	public RewriteCallback() {
	    // DO NOTHING.
	}

	/** {@inheritDoc} */
	@Override
	public void call(ProximityQuery node, RewriteContext context) {
	    ProximityQuery rewritten = (ProximityQuery) node.rewrite(context.getIndexReader());
	    ProximityQuery parent = node.getParent();
	    if (parent != null) {
		int index = parent.getIndexOf(node);
		parent.setChildAt(index, rewritten);
	    } else {
		context.setRoot(rewritten);
	    }
	}
    }

    /**
     * Constructor.
     */
    public ProximityQuery(String field, String queryText, int slop, boolean inOrder) {
	super();
	this.field = field;
	this.queryTest = queryText;
	this.slop = slop;
	this.inOrder = inOrder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query rewrite(IndexReader reader) throws IOException {
	ASTVisitor<ProximityQuery> visitor = new ASTVisitor<ProximityQuery>();
	visitor.postorder(this, new RewriteCallback(), new RewriteContext(reader, this));
	return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addChild(ProximityQuery child) {
	this.children.add(child);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ProximityQuery> getChildren() {
	return this.children;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setChildAt(int index, ProximityQuery child) {
	this.children.add(index, child);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProximityQuery getChildAt(int index) {
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
    public int getIndexOf(ProximityQuery child) {
	return this.children.indexOf(child);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParent(ProximityQuery parent) {
	this.parent = parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProximityQuery getParent() {
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
     * Sets the field.
     * @param field the field string to be set.
     */
    public void setField(String field) {
	this.field = field;
    }

    /**
     * Rerturns the field.
     * @return the assigned field string.
     */
    public String getField() {
	return this.field;
    }

    /**
     * Sets the slop width.
     * @param slop the slop width to be set.
     */
    public void setSlop(int slop) {
	this.slop = slop;
    }

    /**
     * Returns the slop width.
     * @return the assigned slop width.
     */
    public int getSlop() {
	return this.slop;
    }

    /**
     * Sets the in-order configuration.
     * @param inOrder the in-order configuration to be set.
     */
    public void setInOrder(boolean inOrder) {
	this.inOrder = inOrder;
    }

    /**
     * Returns the assigned in-order configuration.
     * @return the assigned in-order configuration.
     */
    public boolean getInOrder() {
	return this.inOrder;
    }
}
