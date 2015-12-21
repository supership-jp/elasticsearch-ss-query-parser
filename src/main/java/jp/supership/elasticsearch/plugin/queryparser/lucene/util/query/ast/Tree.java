/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.ast;

import java.util.List;

/**
 * This interface specifies the implementing class has ability to handle abstract syntax trees
 * for the ambiguous query families.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface Tree<T> {
    /**
     * Represents internal tree model.
     */
    public static interface Model<T> {
	/**
	 * Returns the model's root node.
	 * @return the root node path of the model.
	 */
	public TreePath<T> getRoot();

	/**
	 * Returns the model's current node.
	 * @return the current node path of the model.
	 */
	public TreePath<T> getCurrent();

	/**
	 * Sets the model's current node.
	 * @param current the current node's path to be set.
	 */
	public void setCurrent(TreePath<T> current);

	/**
	 * Returns the model's current node's left-most child.
	 * @return the current node's left-most child's path of the model.
	 */
	public TreePath<T> getLeftMost();

	/**
	 * Returns the model's current node's right-most child.
	 * @return the current node's right-most child's path of the model.
	 */
	public TreePath<T> getRightMost();

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
    }

    /**
     * Registers the assigened listener to the handling tree.
     * @param listener the listener implemantation to be registered in.
     */
    public void addListener(TreeEventListener listener);

    /**
     * Returns the child AST of the current path at the given index.
     * @param  index the target child's index.
     * @return the index-th child node of the parent.
     */
    public T getChildAt(int index);

    /**
     * Returns the child AST of specified parent at the given index.
     * @param  parent the target parent.
     * @param  index the target child's index.
     * @return the index-th child node of the parent.
     */
    public T getChildAt(T parent, int index);

    /**
     * Returns the number of children AST node the current path's node contains.
     * @return the total number of the children.
     */
    public int getChildCount();

    /**
     * Returns the number of children AST node the parent contains.
     * @param  parent the target parent.
     * @return the total number of the children.
     */
    public int getChildCount(T parent);

    /**
     * Returns the index of node in the current path node's children.
     * @param  child the target child.
     * @return the index of the given child.
     */
    public int getIndexOf(T child);

    /**
     * Returns the index of node in the parent children.
     * @param  parent the target parent.
     * @param  child the target child.
     * @return the index of the given child.
     */
    public int getIndexOf(T parent, T child);

    /**
     * Returns the root node of the tree.
     * @return the root node of the tree.
     */
    public T getRoot();

    /**
     * Returns true if the current path's node is a leaf.
     * @return true if the current path's node is a leaf.
     */
    public boolean isLeaf();

    /**
     * Returns true if the node is a leaf.
     * @return true if the node is a leaf.
     */
    public boolean isLeaf(T node);
}
