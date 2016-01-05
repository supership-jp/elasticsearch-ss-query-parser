/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.spans.xst;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.ExternalQueryParser;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.InternalQueryParser;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.xst.Tree;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.xst.TreeEvent;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.xst.TreeEventListener;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.xst.TreeEventNotifier;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.xst.TreePath;

/**
 * This interface specifies the implementing class has ability to handle abstract syntax trees
 * for the ambiguous query families.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class ConcreteSyntaxTree implements Tree<Fragment>, TreeEventNotifier {
    /**
     * Represents parsing phase context.
     */
    private class Model implements Tree.Model<Fragment> {
	/** Holds each nodes' states as hashtable. */
	private Hashtable<TreePath<Fragment>, Fragment.State> states = new Hashtable<TreePath<Fragment>, Fragment.State>();

	/** Holds the marked nodes' path. */
	private Stack<TreePath<Fragment>> marks = new Stack<TreePath<Fragment>>();

	/** Holds the root node's path. */
	private TreePath<Fragment> root;

	/** Holds the current node's path. */
	private transient TreePath<Fragment> current;

	/** Holds the current node's left-most child's path. */
	private transient TreePath<Fragment> leftMost;

	/** Holds the current node's right-most child's path. */
	private transient TreePath<Fragment> rightMost;

	/**
	 * Constructor.
	 */
	public Model(Fragment root) {
	    this(new TreePath<Fragment>(root));
	}

	/**
	 * Constructor.
	 */
	public Model(Fragment root, Fragment.State state) {
	    this(new TreePath<Fragment>(root), state);
	}

	/**
	 * Constructor.
	 */
	public Model(TreePath<Fragment> root) {
	    this(root, new Fragment.State());
	}

	/**
	 * Constructor.
	 */
	public Model(TreePath<Fragment> root, Fragment.State state) {
	    this.root = root;
	    this.current = root;
	    this.put(root, state);
	}

	/**
	 * Registers the given node to the model with default state.
	 * @param node the concerning node.
	 */
	public void put(Fragment node) {
	    this.put(this.current.getPathTo(node));
	}

	/**
	 * Registers the given node to the model with the specified state.
	 * @param node the concerning node.
	 * @param state the concerning node's state.
	 */
	public void put(Fragment node, Fragment.State state) {
	    this.put(this.current.getPathTo(node), state);
	}

	/**
	 * Registers the given tree path to the model with default state.
	 * @param path the concerning node's path.
	 */
	public void put(TreePath<Fragment> path) {
	    this.put(path, new Fragment.State());
	}

	/**
	 * Registers the given tree path to the model with default state.
	 * @param path the concerning node's path.
	 * @param state the concerning node's state.
	 */
	public void put(TreePath<Fragment> path, Fragment.State state) {
	    Fragment node = path.getTail();
	    node.setTreePath(path);
	    TreePath<Fragment> parentPath = path.getParent();
	    if (parentPath != null) {
		Fragment parent = parentPath.getTail();
		int index = parent.getIndexOf(node);
		if (index == -1) {
		    parent.addChild(node);
		}
	    }
	    this.states.put(path, state);
	}

	/**
	 * Returns the bundled state of the specified path.
	 * @param  path the concerning node's path.
	 * @return the state of the node which is specified with the given path.
	 */
	public Fragment.State getStateOf(TreePath<Fragment> path) {
	    return this.states.get(path);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TreePath<Fragment> getRoot() {
	    return this.root;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TreePath<Fragment> getCurrent() {
	    return this.current;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCurrent(TreePath<Fragment> current) {
	    this.current = current;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TreePath<Fragment> getLeftMost() {
	    if (this.leftMost == null) {
		Fragment node = this.current.getTail();
		if (node.isLeaf()) {
		    this.leftMost = null;
		} else {
		    this.leftMost = this.current.getPathTo(node.getChildAt(0));
		}
	    }
	    return this.leftMost;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TreePath<Fragment> getRightMost() {
	    if (this.rightMost == null) {
		Fragment node = this.current.getTail();
		if (node.isLeaf()) {
		    this.rightMost = null;
		} else {
		    this.rightMost = this.current.getPathTo(node.getChildAt(node.getChildCount() - 1));
		}
	    }
	    return this.rightMost;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void ascend(boolean refresh) {
	    Fragment parent = this.current.getTail().getParent();
	    if (parent != null) {
		int candidate = this.marks.search(this.current);
		if (refresh && candidate > 0) {
		    this.marks.setSize(candidate - 1);
		}
		this.current = this.current.getParent();
	    }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void descend(int index, boolean mark) {
	    TreePath<Fragment> candidate = this.getLeftMost();
	    if (candidate != null) {
		if (mark) {
		    this.mark();
		}
		this.setCurrent(candidate);
	    }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void mark() {
	    this.marks.push(this.getCurrent());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rewind() {
	    if (!this.marks.empty()) {
		this.current = this.marks.pop();
	    }
	}
    }

    /**
     * This class handles transformaytions of the tree structure.
     */
    private class TreeTransformationHandler implements TreeEventListener<Fragment> {
	/** Holds bundled model. */
	private Model model;

	/**
	 * Constructor.
	 */
	public TreeTransformationHandler(Model model) {
	    this.model = model;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNodesChanged(TreeEvent<Fragment> event) {
	    // DO NOTHING.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNodesInserted(TreeEvent<Fragment> event) {
	    @SuppressWarnings("unckecked")
	    Fragment source = (Fragment) event.getSource();
	    TreePath<Fragment> parentPath = event.getPath().getParent();
	    if (parentPath != null) {
		Fragment.State parentState = this.model.getStateOf(parentPath);
		if (source.getOperator() == InternalQueryParser.MODIFIER_NEGATE) {
		    parentState.isNotQuery(true);
		}
	    }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNodesRemoved(TreeEvent<Fragment> event) {
	    @SuppressWarnings("unckecked")
	    Fragment source = (Fragment) event.getSource();
	    TreePath<Fragment> parentPath = event.getPath().getParent();
	    if (parentPath != null && source.getOperator() == InternalQueryParser.MODIFIER_NEGATE) {
		Fragment parent = parentPath.getTail();
		boolean isNotQuery = false;
		for (Fragment child : parent.getChildren()) {
		    if (child.getOperator() == InternalQueryParser.MODIFIER_NEGATE) {
			isNotQuery = true;
		    }
		}
		Fragment.State parentState = this.model.getStateOf(parentPath);
		parentState.isNotQuery(isNotQuery);
	    }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onPathAscended(TreeEvent<Fragment> event) {
	    // DO NOTHING.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onPathDescended(TreeEvent<Fragment> event) {
	    // DO NOTHING.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onTransduced(TreeEvent<Fragment> event) {
	    // DO NOTHING.
	}
    }

    /** Holds event listeners. */
    private List<TreeEventListener> listeners = new ArrayList<TreeEventListener>();

    /** Holds tree model. */
    private Model model;

    /**
     * Constructor.
     */
    public ConcreteSyntaxTree() {
	// The following parameters means nothind, because the root node will be managed as a SpanOrQuery,
	// hence the given parameters will be ignored.
	this(new Fragment(false, -1, -1, false));
    }

    /**
     * Constructor.
     */
    public ConcreteSyntaxTree(boolean infixed, int slop, int operator, boolean inOrder) {
	this(new Fragment(infixed, slop, operator, inOrder));
    }

    /**
     * Constructor.
     */
    public ConcreteSyntaxTree(Fragment root) {
	Fragment.State state = new Fragment.State();
	state.isOrQuery(true);
	this.model = new Model(new TreePath<Fragment>(root), state);
	this.addListener(new TreeTransformationHandler(this.model));
    }

    /**
     * Returns the handling model.
     * @return the bundled model of this tree.
     */
    protected Model getModel() {
	return this.model;
    }

    /**
     * Returns the bundled state of the specified path.
     * @param  path the concerning node's path.
     * @return the state of the node which is specified with the given path.
     */
    public Fragment.State getStateOf(TreePath<Fragment> path) {
	return this.getModel().getStateOf(path);
    }

    /**
     * Inserts the given node into the model.
     * @param node the node to be inserted.
     */
    public void insert(Fragment node) {
	this.insert(node, false, false);
    }

    /**
     * Inserts the given node into the model.
     * @param node the node to be inserted.
     */
    public void insert(Fragment node, Fragment.State state) {
	this.insert(node, state, false, false);
    }

    /**
     * Inserts the given node into the model.
     * @param node the node to be inserted.
     * @param proceed if this value is set to be true, the current path proceeds to the very new node's one.
     */
    public void insert(Fragment node, boolean proceed) {
	this.insert(node, proceed, false);
    }

    /**
     * Inserts the given node into the model.
     * @param node the node to be inserted.
     * @param state the node state to be inserted.
     * @param proceed if this value is set to be true, the current path proceeds to the very new node's one.
     */
    public void insert(Fragment node, Fragment.State state, boolean proceed) {
	this.insert(node, state, proceed, false);
    }

    /**
     * Inserts the given node into the model.
     * @param node the node to be inserted.
     * @param proceed if this value is set to be true, the current path proceeds to the very new node's one.
     * @param mark if this value is set to be true, the previous path is marked.
     */
    public void insert(Fragment node, boolean proceed, boolean mark) {
	this.insert(this.getModel().getCurrent().getPathTo(node), proceed, mark);
    }

    /**
     * Inserts the given node into the model.
     * @param node the node to be inserted.
     * @param state the node state to be inserted.
     * @param proceed if this value is set to be true, the current path proceeds to the very new node's one.
     * @param mark if this value is set to be true, the previous path is marked.
     */
    public void insert(Fragment node, Fragment.State state, boolean proceed, boolean mark) {
	this.insert(this.getModel().getCurrent().getPathTo(node), state, proceed, mark);
    }

    /**
     * Inserts the given path into the model.
     * @param path the node path to be inserted.
     * @param proceed if this value is set to be true, the current path proceeds to the very new node's one.
     * @param mark if this value is set to be true, the previous path is marked.
     */
    public void insert(TreePath<Fragment> path, boolean proceed, boolean mark) {
	if (mark) {
	    this.getModel().mark();
	}
	this.getModel().put(path);
	this.fireNodesInserted(new TreeEvent<Fragment>(path.getTail(), path));
	if (proceed) {
	    this.getModel().setCurrent(path);
	}
    }

    /**
     * Inserts the given path into the model.
     * @param path the node path to be inserted.
     * @param state the node state to be inserted.
     * @param proceed if this value is set to be true, the current path proceeds to the very new node's one.
     * @param mark if this value is set to be true, the previous path is marked.
     */
    public void insert(TreePath<Fragment> path, Fragment.State state, boolean proceed, boolean mark) {
	if (mark) {
	    this.getModel().mark();
	}
	this.getModel().put(path, state);
	this.fireNodesInserted(new TreeEvent<Fragment>(path.getTail(), path));
	if (proceed) {
	    this.getModel().setCurrent(path);
	}
    }

    /**
     * Ascends to the current path's parent.
     */
    public void ascend() {
	this.ascend(true);
    }

    /**
     * Ascends to the current path's child with specified index.
     * @param index the index of the target child node.
     */
    public void descend(int index) {
	this.descend(index, true);
    }

    /**
     * Ascends to the current path's parent.
     * @param refresh if this value is set to be true, the preceeding marks after the current node will be vanished.
     */
    public void ascend(boolean refresh) {
	TreePath<Fragment> path = this.getModel().getCurrent();
	this.getModel().ascend(refresh);
	this.firePathAscended(new TreeEvent<Fragment>(path.getTail(), path));
    }

    /**
     * Ascends to the current path's child with specified index.
     * @param index the index of the target child node.
     * @param mark if this value is set to be true, the previous node will be marked.
     */
    public void descend(int index, boolean mark) {
	TreePath<Fragment> path = this.getModel().getCurrent();
	this.getModel().descend(index, mark);
	this.firePathDescended(new TreeEvent<Fragment>(path.getTail(), path));
    }

    /**
     * Marks the currently handling node.
     */
    public void mark() {
	this.getModel().mark();
    }

    /**
     * Rewinds the currently handling node.
     */
    public void rewind() {
	this.getModel().rewind();
    }

    /**
     * Rewinds the currently handling node.
     */
    public void rewindAll() {
	while (this.getModel().getCurrent().getParent() != null) {
	    this.getModel().rewind();
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(TreeEventListener listener) {
	this.listeners.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Fragment getChildAt(int index) {
	return this.getModel().getCurrent().getTail().getChildAt(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Fragment getChildAt(Fragment parent, int index) {
	return parent.getChildAt(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getChildCount() {
	return this.getModel().getCurrent().getTail().getChildCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getChildCount(Fragment parent) {
	return parent.getChildCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIndexOf(Fragment child) {
	return this.getModel().getCurrent().getTail().getIndexOf(child);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIndexOf(Fragment parent, Fragment child) {
	return parent.getIndexOf(child);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Fragment getRoot() {
	return this.getModel().getRoot().getTail();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isLeaf() {
	return this.getModel().getCurrent().getTail().isLeaf();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isLeaf(Fragment node) {
	return node.isLeaf();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fireNodesChanged(TreeEvent event) {
	for (TreeEventListener listener : this.listeners) {
	    listener.onNodesChanged(event);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fireNodesInserted(TreeEvent event) {
	for (TreeEventListener listener : this.listeners) {
	    listener.onNodesInserted(event);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fireNodesRemoved(TreeEvent event) {
	for (TreeEventListener listener : this.listeners) {
	    listener.onNodesRemoved(event);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void firePathAscended(TreeEvent event) {
	for (TreeEventListener listener : this.listeners) {
	    listener.onPathAscended(event);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void firePathDescended(TreeEvent event) {
	for (TreeEventListener listener : this.listeners) {
	    listener.onPathDescended(event);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fireTransduced(TreeEvent event) {
	for (TreeEventListener listener : this.listeners) {
	    listener.onTransduced(event);
	}
    }
}
