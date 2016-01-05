/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.xst;

import java.io.Serializable;
import java.util.Arrays;

/**
 * This class represents a sequence of tree nodes that form a path starting from the root to the node.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class TreePath<T> implements Serializable {
    /** Holds the last path element, i.e., the self-representive node. */
    private Object tail;

    /** Holds parent path to be reused. */
    private transient TreePath<T> parent;

    /**
     * Constructor.
     */
    protected TreePath() {
	// THIS IS PLACEHOLDER.
    }

    /**
     * Constructor.
     */
    public TreePath(T[] path) {
	if (path == null || path.length == 0) {
	    throw new IllegalArgumentException("path in TreePath must not be null and not empty.");
	} else {
	    this.tail = path[path.length - 1];
	    if (this.tail == null) {
		throw new IllegalArgumentException("the last path element must not be null.");
	    }
	    if (path.length > 1) {
		this.parent = new TreePath<T>(path, path.length - 1);
	    }
	}
    }

    /**
     * Constructor.
     */
    public TreePath(T tail) {
	if (tail == null) {
	    throw new IllegalArgumentException("the last path element must not be null.");
	}
	this.tail = tail;
	this.parent = null;
    }

    /**
     * Constructor.
     */
    public TreePath(TreePath<T> parent, T tail) {
	if (tail == null) {
	    throw new IllegalArgumentException("the last path element must not be null.");
	}
	this.tail = tail;
	this.parent = parent;
    }

    /**
     * Constructor.
     */
    public TreePath(T[] path, int length) {
	this.tail = path[length - 1];
	if (this.tail == null) {
	    throw new IllegalArgumentException("the last path element must not be null.");
	}
	if (length > 1) {
	    this.parent = new TreePath<T>(path, length - 1);
	}
    }

    /**
     * Returns the last node in the path, i.e., the self-representive element.
     * @return the very last node in the path.
     */
    public T getTail() {
	@SuppressWarnings("unchecked")
	T result = (T) this.tail;
	return result;
    }

    /**
     * Returns the descending path to this path.
     * @return the very next descending path to this.
     */
    public TreePath<T> getParent() {
	return this.parent;
    }

    /**
     * Returns the number of nodes within the path.
     * @return the number of nodes within the path.
     */
    public int getLength() {
	int result = 0;
	for (TreePath<T> path = this; path != null; path = path.getParent()) {
	    result++;
	}
	return result;
    }

    /**
     * Returns the internal path expression.
     * @return the internal path expression as an array.
     */
    public T[] getPath() {
	int i = this.getLength();
	Object[] objects = new Object[i--];
	for (TreePath<T> path = this; path != null; path = path.getParent()) {
	    objects[i--] = path.getTail();
	}
	@SuppressWarnings("unchecked")
	T[] result = (T[]) objects;
        return result;
   } 

    /**
     * Returns the given index's node within the path.
     * @param  index the index which is concerned.
     * @return the assigned index's node within the path.
     */
    public T getElementAt(int index) {
	int length = this.getLength();
	if (index < 0 || index >= length) {
	    throw new IllegalArgumentException("invalid index: " + index);
	}

	TreePath<T> path = this;
	for (int i = length - 1; i != index; i--) {
	    path = path.getParent();
	}

	return path.getTail();
    }

    /**
     * Returns true if the path is considered to be the descendant path of the given path.
     * @param  that the comparing path.
     * @return true if the path is considered to be the dexcendant of the given path.
     */
    public boolean isDescendantTo(TreePath<T> that) {
	if (that == this) {
	    return true;
	}
	if (that != null) {
	    int thisLength = this.getLength();
	    int thatLength = that.getLength();
	    if (thatLength < thisLength) {
		return false;
	    }
	    while (thatLength-- > thisLength) {
		that = that.getParent();
	    }
	    return this.equals(that);
	}
	return false;
    }

    /**
     * Returns the descending path to the given node.
     * @param  node the descending node.
     * @return the descending path to the given node.
     */
    public TreePath<T> getPathTo(T target) {
	if (target == null) {
	    throw new NullPointerException("null target is not allowed.");
	}
	return new TreePath<T>(this, target);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
	return this.getTail().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object object) {
	if (object == this) {
	    return true;
	}

	if (object instanceof TreePath) {
	    TreePath<T> that = (TreePath<T>) object;
	    if (this.getLength() != that.getLength()) {
		return false;
	    }
	    for (TreePath<T> path = this; path != null; path = path.getParent()) {
		if (!(path.getTail().equals(that.getTail()))) {
		    return false;
		}
		that = that.getParent();
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
	StringBuilder builder = new StringBuilder("[");
	for (int index = 0, length = this.getLength(); index < length; index++) {
	    if (index > 0) {
		builder.append(",");
	    }
	    builder.append(this.getElementAt(index));
	}
	builder.append("]");
	return builder.toString();
    }
}
