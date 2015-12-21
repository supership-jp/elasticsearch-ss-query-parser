/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.ast;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * This interface specifies the implementing class has ability to handle abstract syntax tree
 * traversals.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class TreeVisitor<N extends Node> {
    /** This interfaces' implementation is responsible to hold some common context within traversal. */
    public static class Context {
	// THIS IS PLACEHOLDER.
    }

    /** Represents callback functions which will be fired on visit events. */
    public interface Callback<C extends Context> {
	/** This method will be called when node visit event has been fired. */
	public void call(N node, C context);
    }

    /** Provides functionality to traverse in preorder. */
    private class PreorderIterator<N> implements Iterator<N>, Iterable<N> {
	/** Holds internal buffer. */
	private Stack<N> visiting;
	/** Holds tree root. */
	private N root;

	/**
	 * Constructor.
	 */
	public PreorderIterator(N root) {
	    this.root = root;
	    this.visiting.push(this.root);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<N> iterator() {
	    return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
	    return !this.visiting.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public N next() {
	    N result = this.visiting.pop();
	    int childCount = result.getChildCount();
	    if (childCount > 0) {
		for (int i = childCount; i > -1; i--) {
		    this.visiting.push(result.getChildAt(i));
		}
	    }
	    return result;
	}
    }

    /** Provides functionality to traverse in postorder. */
    private class PostorderIterator<N> implements Iterator<N>, Iterable<N> {
	/** Holds internal buffer. */
	private N nextNode;
	/** Holds tree root. */
	private N root;

	/**
	 * Constructor.
	 */
	public PostorderIterator(N root) {
	    this.root = root;
	    this.nextNode = this.root;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<N> iterator() {
	    return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
	    return this.nextNode != null;
	}

	/**
	 * Returns the right sibling of the assigned node.
	 * @param  node the node to be checked.
	 * @return the right sibling of the given node.
	 */
	private N getRightSiblingOf(N node) {
	    N parent = node.getParent();
	    if (parent == null) {
		return null;
	    } else {
		int childCount = parent.getChildCount();
		int index = parent.getIndexOf(node);
		if (index < childCount - 1) {
		    return parent.getChildAt(index + 1);
		} else {
		    return null;
		}
	    }
	}

	/**
	 * Returns the next node of the given one in postorder.
	 * @param  node the node to be checked.
	 * @return the next node of the given one in postorder.
	 */
	private N nextOf(N node) {
	    N rightSibling = this.getRightSiblingOf(node);
	    if (rightSibling != null) {
		N current = rightSibling;
		while (current.getChildCount() > 0) {
		    current = current.getChildAt(0);
		}
		return current;
	    } else {
		N parent = node.getParent();
		if (parent != null) {
		    return parent;
		} else {
		    return null;
		}
	    }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public N next() {
	    N result = this.nextNode;
	    this.nextNode = this.nextOf(this.nextNode);
	    return result;
	}
    }

    /**
     * Constructor.
     */
    public TreeVisitor() {
	// DO NOTHING.
    }

    /**
     * Traverses the given AST in preorder with given callback and context.
     * @param root the root node of the argumenting AST.
     * @param callback the callback function to be called when the visitor visits the node.
     * @param context the context to be handled within the traversal.
     */
    public void preorder(N root, Callback callback, Context context) {
	PreorderIterator<N> flatten = new PreorderIterator<N>(root);
	for (N node : flatten) {
	    callback.call(node, context);
	}
    }

    /**
     * Traverses the given AST in postorder with given callback and context.
     * @param root the root node of the argumenting AST.
     * @param callback the callback function to be called when the visitor visits the node.
     * @param context the context to be handled within the traversal.
     */
    public void postorder(N root, Callback callback, Context context) {
	PostorderIterator<N> flatten = new PostorderIterator<N>(root);
	for (N node : flatten) {
	    callback.call(node, context);
	}
    }
}
