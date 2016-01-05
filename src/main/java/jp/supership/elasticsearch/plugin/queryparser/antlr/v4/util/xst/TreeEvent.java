/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.xst;

import java.util.EventObject;

/**
 * An event used to signal a state change for an object.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class TreeEvent<N extends Node> extends EventObject {
    /** Holds event source node's tree path. */
    private TreePath<N> path;
    
    /**
     * Constructor.
     */
    public TreeEvent(Object source) {
	super(source);
    }

    /**
     * Constructor.
     */
    public TreeEvent(Object source, TreePath<N> path) {
	super(source);
	this.path = path;
    }

    /**
     * Sets the event source node's path.
     * @param path the event source node's path.
     */
    public void setPath(TreePath<N> path) {
	this.path = path;
    }

    /**
     * Returns the event source node's path.
     * @return the event source node's path.
     */
    public TreePath<N> getPath() {
	return this.path;
    }
}
