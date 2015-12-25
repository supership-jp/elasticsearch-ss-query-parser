/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.ast;

import java.io.Serializable;
import java.util.Arrays;

/**
 * This class represents a sequence of tree nodes that form a path starting from the root to the node.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class TreePath<T> implements Serializable {
    /** Holds actual path which consists of node array. */
    private Object[] path;

    /** Holds parent path to be reused. */
    private transient TreePath<T> parentPath;

    /**
     * Constructor.
     */
    public TreePath() {
	this.path = new Object[0];
    }

    /**
     * Constructor.
     */
    public TreePath(T[] path) {
	if (path == null) {
	    throw new IllegalArgumentException("null path not allowed.");
	}
	this.path = new Object[path.length];
	System.arraycopy(path, 0, this.path, 0, path.length);
    }

    /**
     * Constructor.
     */
    public TreePath(T node) {
	this.path = new Object[1];
	this.path[0] = node;
    }

    /**
     * Constructor.
     */
    public TreePath(TreePath<T> path, T node) {
	if (node == null) {
	    throw new NullPointerException("null element not allowed.");
	}
	Object[] treePath = path.getPath();
	this.path = new Object[treePath.length + 1];
	System.arraycopy(treePath, 0, this.path, 0, treePath.length);
	this.path[treePath.length] = node;
    }

    /**
     * Constructor.
     */
    public TreePath(T[] path, int length) {
	this.path = new Object[length];
	System.arraycopy(path, 0, this.path, 0, length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
	return this.getLastPathElement().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object that) {
	if (that instanceof TreePath) {
	    @SuppressWarnings("unchecked")
	    T[] treePath = ((TreePath<T>) that).getPath();
	    if (treePath.length != this.path.length) {
		return false;
	    }
	    for (int i = 0; i < this.path.length; i++) {
		if (!((T) this.path[i]).equals(treePath[i])) {
		    return false;
		}
	    }
	    return true;
	}
	return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
	if (this.path.length == 1) {
	    return String.valueOf(path[0]);
	} else {
	    return Arrays.asList(this.path).toString();
	}
    }

    /**
     * Returns the internal path expression.
     * @return the internal path expression as an array.
     */
    public T[] getPath() {
	@SuppressWarnings("unchecked")
	T[] result = (T[]) this.path;
        return result;
    }

    /**
     * Returns the last node in the path.
     * @return the very last node in the path.
     */
    public T getLastPathElement() {
	@SuppressWarnings("unchecked")
	T result = (T) this.path[this.path.length - 1];
	return result;
    }

    /**
     * Returns the number of nodes within the path.
     * @return the number of nodes within the path.
     */
    public int getPathCount() {
	return this.path.length;
    }

    /**
     * Returns the given index's node within the path.
     * @param  index the index which is concerned.
     * @return the assigned index's node within the path.
     */
    public T getPathElement(int index) {
	if (index < 0 || index >= this.getPathCount()) {
	    throw new IllegalArgumentException("invalid index: " + index);
	}
	@SuppressWarnings("unchecked")
	T result = (T) this.path[index];
	return result;
    }

    /**
     * Returns true if the path is considered to be the descendant path of the given path.
     * @param  path the comparing path.
     * @return true if the path is considered to be the dexcendant of the given path.
     */
    public boolean isDescendantTo(TreePath<T> path) {
	if (path == null) {
	    return false;
	}
	int count = this.getPathCount();
	int otherCount = path.getPathCount();
	if (otherCount < count) {
	    return false;
	}
	while (otherCount > count) {
	    otherCount--;
	    path = path.getParentPath();
	}
	return this.equals(path);
    }

    /**
     * Returns the descending path to the given node.
     * @param  node the descending node.
     * @return the descending path to the given node.
     */
    public TreePath<T> getPathTo(T node) {
	return new TreePath<T>(this, node);
    }

    /**
     * Returns the descending path to this path.
     * @return the very next descending path to this.
     */
    public TreePath<T> getParentPath() {
	if (this.path.length <= 1) {
	    return null;
	}
	if (this.parentPath == null) {
	    parentPath = new TreePath<T>(this.getPath(), this.path.length - 1);
	}
	return this.parentPath;
    }
}
