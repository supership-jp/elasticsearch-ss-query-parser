/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util;

import java.io.Reader;
import org.apache.lucene.search.Query;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.spans.xst.Fragment;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.spans.xst.ConcreteSyntaxTree;

/**
 * This interface specifies the implementing class can handle the given {@code java.io.Reader}
 * as raw query string and instanciates {@code org.apache.lucene.search.spans.SpanQuery}.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface TreeHandler extends QueryHandler {
    /**
     * Represents query handling context, i.e., in accordance to this instance's state, appropriate
     * {@code Query} will be instanciated.
     */
    public class Context {
	/** Holds currently handling field name. */
	private String field = null;
	/** Holds currently handling term token. */
	private String term = null;

	/** Returns the currently handling field. */
	public String getField() {
	    return this.field;
	}

	/** Sets the currently handling field. */
	public void setField(String field) {
	    this.field = field;
	}

	/** Return the currently handling term. */
	public String getTerm() {
	    return this.term;
	}

	/** Sets the currently handling term. */
	public void setTerm(String term) {
	    this.term = term;
	}

	/** Clears currently handling properties. */
	public void clear() {
	    this.field = null;
	    this.term = null;
	}
    }

    /**
     * Returns the root of the currently handling tree.
     */
    public Fragment getRoot();

    /**
     * Ascends to the current tree path's parent.
     * @param refresh if this value is set to be true, marked path rewinds to the appropriate point.
     */
    public void ascend(boolean refresh);

    /**
     * Descends to the current tree path's child of the specified index.
     * @param index the index of the child node which is attempted to descend.
     * @param mark if this value is set to be true, the tree path before descending is marked.
     */
    public void descend(int index, boolean mark);

    /**
     * Marks the currently handling node.
     */
    public void mark();

    /**
     * Rewinds the currently handling node.
     */
    public void rewind();

    /**
     * Clears the currently handling tree and the assigned properties.
     * @param clear if this value is set to be true, the previously assigned properties will be palished.
     */
    public void clear();

    /**
     * Inserts the given node into the model.
     * @param node the node to be inserted.
     */
    public void insert(Fragment node);

    /**
     * Inserts the given node into the model.
     * @param node the node to be inserted.
     */
    public void insert(Fragment node, Fragment.Tag tag);
}
