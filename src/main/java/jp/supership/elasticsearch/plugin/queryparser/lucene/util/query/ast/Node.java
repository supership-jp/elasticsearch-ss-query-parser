/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.ast;

import java.util.List;

/**
 * This interface specifies the implementing class has ability to handle abstract syntax tree nodes
 * for the ambiguous query families.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface Node<N extends Node> {
    /**
     * Reprents each nodes' state.
     */
    public static class State {
	/** Holds true if the node was removed. */
	private boolean isRemoved = false;

	/** Holds true if the node was changed. */
	private boolean isChanged = false;

	/**
	 * Constructor.
	 */
	public State() {
	    // DO NOTHING.
	}

	/**
	 * Returns true if the node was removed.
	 * @return true if the node was removed.
	 */
	public boolean isRemoved() {
	    return this.isRemoved;
	}

	/**
	 * Sets to be true if the node was removed.
	 * @param isRemoved set to be true if the node was removed.
	 */
	public void isRemoved(boolean isRemoved) {
	    this.isRemoved = isRemoved;
	}

	/**
	 * Returns true if the node was changed.
	 * @return true if the node was changed.
	 */
	public boolean isChanged() {
	    return this.isChanged;
	}

	/**
	 * Sets to be true if the node was changed.
	 * @param isChanged set to be true if the node was changed.
	 */
	public void isChanged(boolean isChanged) {
	    this.isChanged = isChanged;
	}
    }

    /**
     * Adds the child AST node to its children.
     * @param child the child node to be set.
     */
    public void addChild(N child);

    /**
     * Returns the children of the node as a list.
     * @return the collection of the associating child nodes.
     */
    public List<N> getChildren();

    /**
     * Sets the child AST node at index.
     * @param index the target child's index.
     * @param child the target child node.
     */
    public void setChildAt(int index, N child);

    /**
     * Returns the child AST node at index.
     * @param  index the target child's index.
     * @return the index-th child node.
     */
    public N getChildAt(int index);

    /**
     * Returns the number of children AST node the node contains.
     * @return the total number of the children.
     */
    public int getChildCount();

    /**
     * Returns the index of node in the node children.
     * @param  child the target child.
     * @return the index of the given child.
     */
    public int getIndexOf(N child);

    /**
     * Sets the parent AST node of the node.
     * @param the parent node to be set.
     */
    public void setParent(N parent);

    /**
     * Returns the parent AST node of the node.
     * @return the parent node.
     */
    public N getParent();

    /**
     * Sets the tree path to this node within the tree.
     * @param treePath the tree path to be set.
     */
    public void setTreePath(TreePath<N> treePath);

    /**
     * Returns the tree path to this node within the tree.
     * @return the tree path to this node within the tree.
     */
    public TreePath<N> getTreePath();

    /**
     * Returns true if the node is a leaf.
     * @return true if the node is a a leaf.
     */
    public boolean isLeaf();
}
