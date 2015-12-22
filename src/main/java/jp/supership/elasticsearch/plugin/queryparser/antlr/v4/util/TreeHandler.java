/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util;

import java.io.Reader;
import org.apache.lucene.search.Query;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.spans.archetype.ProximityArchetype;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.spans.archetype.ProximityArchetypeTree;

/**
 * This interface specifies the implementing class can handle the given {@code java.io.Reader}
 * as raw query string and instanciates {@code org.apache.lucene.search.spans.SpanQuery}.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface TreeHandler {
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
     * Creates {@link org.apache.lucene.search.Query} in accordance with the given raw query string.
     * @param  queryText the raw query string to be parsed.
     * @throws HandleException if the handling fails.
     */
    public Query handle(String queryText) throws HandleException;

    /**
     * Returns the root of the currently handling tree.
     */
    public ProximityArchetype getRoot();

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
     * Forgets currently handling tree and possibly clears the assigned properties.
     * @param clear if this value is set to be true, the previously assigned properties will be palished.
     */
    public void forget(boolean clear);

    /**
     * Inserts the given node into the model.
     * @param node the node to be inserted.
     */
    public void insert(ProximityArchetype node);

    /**
     * Inserts the given node into the model.
     * @param node the node to be inserted.
     */
    public void insert(ProximityArchetype node, ProximityArchetype.State state);

    /**
     * Fetches the given {@link java.io.Reader} to this handler.
     * @param  input the default field for query terms.
     * @throws
     */
    public void fetch(Reader input);
}
