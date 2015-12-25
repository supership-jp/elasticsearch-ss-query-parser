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
class TreeVisitor<N extends Node> {
    /** This interfaces' implementation is responsible to hold some common context within traversal. */
    public static class Context {
	// THIS IS PLACEHOLDER.
    }

    /** Represents callback functions which will be fired on visit events. */
    public static interface Callback<C extends Context> {
	/** This method will be called when node visit event has been fired. */
	public void call(Node node, C context);
    }

    /** Provides functionality to traverse in preorder. */
    private class PreorderIterator implements Iterator<Node>, Iterable<Node> {
	/** Holds internal buffer. */
	private Stack<Node> visiting;
	/** Holds tree root. */
	private Node root;

	/**
	 * Constructor.
	 */
	public PreorderIterator(Node root) {
	    this.root = root;
	    this.visiting.push(this.root);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Node> iterator() {
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
	public void remove() {
	    // DO NOTHING.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Node next() {
	    Node result = this.visiting.pop();
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
    private class PostorderIterator implements Iterator<Node>, Iterable<Node> {
	/** Holds internal buffer. */
	private Node nextNode;
	/** Holds tree root. */
	private Node root;

	/**
	 * Constructor.
	 */
	public PostorderIterator(Node root) {
	    this.root = root;
	    this.nextNode = this.root;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Node> iterator() {
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
	 * {@inheritDoc}
	 */
	@Override
	public void remove() {
	    // DO NOTHING.
	}

	/**
	 * Returns the right sibling of the assigned node.
	 * @param  node the node to be checked.
	 * @return the right sibling of the given node.
	 */
	private Node getRightSiblingOf(Node node) {
	    Node parent = node.getParent();
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
	private Node nextOf(Node node) {
	    Node rightSibling = this.getRightSiblingOf(node);
	    if (rightSibling != null) {
		Node current = rightSibling;
		while (current.getChildCount() > 0) {
		    current = current.getChildAt(0);
		}
		return current;
	    } else {
		Node parent = node.getParent();
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
	public Node next() {
	    Node result = this.nextNode;
	    this.nextNode = this.nextOf(this.nextNode);
	    return result;
	}
    }

    /**
     * Traverses the given AST in preorder with given callback and context.
     * @param root the root node of the argumenting AST.
     * @param callback the callback function to be called when the visitor visits the node.
     * @param context the context to be handled within the traversal.
     */
    protected void preorder(N root, Callback callback, Context context) {
	PreorderIterator flatten = new PreorderIterator(root);
	for (Node node : flatten) {
	    callback.call(node, context);
	}
    }

    /**
     * Traverses the given AST in postorder with given callback and context.
     * @param root the root node of the argumenting AST.
     * @param callback the callback function to be called when the visitor visits the node.
     * @param context the context to be handled within the traversal.
     */
    protected void postorder(N root, Callback callback, Context context) {
	PostorderIterator flatten = new PostorderIterator(root);
	for (Node node : flatten) {
	    callback.call(node, context);
	}
    }
}
