/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.query;

import java.util.List;

/**
 * This interface specifies the implementing class has ability to handle abstract syntax trees
 * for the span query families.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface AST<T extends AST> {
    /**
     * Adds the child AST of the reciever.
     * @param child the child node to be set.
     */
    public void addChild(T child);

    /**
     * Returns the children of the receiver as an Iterator.
     * @return the collection of the associating child nodes.
     */
    public List<T> getChildren();

    /**
     * Returns the child AST at index childIndex.
     * @param  index the target child's index.
     * @return the index-th child node.
     */
    public T getChildAt(int index);

    /**
     * Returns the number of children AST the receiver contains.
     * @return the total number of the children.
     */
    public int getChildCount();

    /**
     * Returns the index of node in the receivers children.
     * @param  child the target child.
     * @return the index of the given child.
     */
    public int getIndexOf(T child);

    /**
     * Sets the parent AST of the receiver.
     * @param the parent node to be set.
     */
    public void setParent(T parent);

    /**
     * Returns the parent AST of the receiver.
     * @return the parent node.
     */
    public T getParent();

    /**
     * Returns true if the receiver is a leaf.
     * @return true if the reciever is a a leaf.
     */
    public boolean isLeaf();
}
