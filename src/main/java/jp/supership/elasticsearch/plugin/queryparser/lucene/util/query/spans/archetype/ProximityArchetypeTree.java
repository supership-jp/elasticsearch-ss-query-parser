/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.spans.archetype;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.ExternalQueryParser;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.dsl.InternalQueryParser;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.ast.Tree;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.ast.TreeEvent;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.ast.TreeEventListener;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.ast.TreeEventNotifier;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.ast.TreePath;

/**
 * This interface specifies the implementing class has ability to handle abstract syntax trees
 * for the ambiguous query families.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class ProximityArchetypeTree implements Tree<ProximityArchetype>, TreeEventNotifier {
    /**
     * Represents parsing phase context.
     */
    private class Model implements Tree<ProximityArchetype>.Model {
	/** Holds each nodes' states as hashtable. */
	private Hashtable<TreePath<ProximityArchetype>, ProximityArchetype.State> states = new Hashtable<TreePath<ProximityArchetype>, ProximityArchetype.State>();

	/** Holds the marked nodes' path. */
	private Stack<TreePath<ProximityArchetype>> marks = new Stack<TreePath<ProximityArchetype>>();

	/** Holds the root node's path. */
	private TreePath<ProximityArchetype> root;

	/** Holds the current node's path. */
	private transient TreePath<ProximityArchetype> current;

	/** Holds the current node's left-most child's path. */
	private transient TreePath<ProximityArchetype> leftMost;

	/**
	 * Constructor.
	 */
	public Model(ProximityArchetype root) {
	    this(new TreePath<ProximityArchetype>(root));
	}

	/**
	 * Constructor.
	 */
	public Model(ProximityArchetype root, ProximityArchetype.State state) {
	    this(new TreePath<ProximityArchetype>(root), state);
	}

	/**
	 * Constructor.
	 */
	public Model(TreePath<ProximityArchetype> root) {
	    this(root, new ProximityArchetype.State());
	}

	/**
	 * Constructor.
	 */
	public Model(TreePath<ProximityArchetype> root, ProximityArchetype.State state) {
	    this.root = root;
	    this.current = root;
	    this.states.put(root, state);
	}

	/**
	 * Registers the given node to the model with default state.
	 * @param node the concerning node.
	 */
	public void put(ProximityArchetype node) {
	    this.put(this.current.getPathTo(node));
	}

	/**
	 * Registers the given node to the model with the specified state.
	 * @param node the concerning node.
	 * @param state the concerning node's state.
	 */
	public void put(ProximityArchetype node, ProximityArchetype.State state) {
	    this.states.put(this.current.getPathTo(node), state);
	}

	/**
	 * Registers the given tree path to the model with default state.
	 * @param path the concerning node's path.
	 * @param state the concerning node's state.
	 */
	public void put(TreePath<ProximityArchetype> path, ProximityArchetype.State state) {
	    this.states.put(path, state);
	}

	/**
	 * Registers the given tree path to the model with default state.
	 * @param path the concerning node's path.
	 */
	public void put(TreePath<ProximityArchetype> path) {
	    this.put(path, new ProximityArchetype.State());
	}

	/**
	 * Registers the given tree path to the model with default state.
	 * @param path the concerning node's path.
	 * @param state the concerning node's state.
	 */
	public void put(TreePath<ProximityArchetype> path, ProximityArchetype.State state) {
	    this.states.put(path, state);
	}

	/**
	 * Returns the bundled state of the specified path.
	 * @param  path the concerning node's path.
	 * @return the state of the node which is specified with the given path.
	 */
	public ProximityArchetype.State getStateOf(TreePath<ProximityArchetype> path) {
	    return this.states.get(path);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TreePath<ProximityArchetype> getRoot() {
	    return this.root;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TreePath<ProximityArchetype> getCurrent() {
	    return this.current;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TreePath<ProximityArchetype> getLeftMost() {
	    if (this.leftMost == null) {
		ProximityArchetype node = this.current.getLastPathElement();
		if (node.isLeaf()) {
		    this.leftMost = null;
		} else {
		    this.leftMost = this.current.getPathTo(node.getChildAt(node.getChildCount() - 1));
		}
	    }
	    return this.leftMost;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCurrent(TreePath<ProximityArchetype> current) {
	    this.current = current;
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
    private class TreeTransformationHandler implements TreeEventListner {
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
	public void onNodesChanged(TreeEvent<ProximityArchetype> event) {
	    // DO NOTHING.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNodesInserted(TreeEvent<ProximityArchetype> event) {
	    @SurpressWarnigs("unckecked")
	    ProximityArchetype source = (ProximityArchetype) event.getSource();
	    TreePath<ProximityArchetype> parentPath = event.getPath().getParentPath();
	    if (parentPath != null && source.getOperator() == InternalQueryParser.MODIFIER_NEGATE) {
		ProximityArchetype.State parentState = this.model.getStateOf(parentPath);
		parentState.isNotQuery(true);
	    }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onNodesRemoved(TreeEvent<ProximityArcheType> event) {
	    // DO NOTHING.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onPathAscended(TreeEvent<ProximityArchetype> event) {
	    // TODO: IMPLEMENT THIS.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onPathDescended(TreeEvent<ProximityArchetype> event) {
	    // TODO: IMPLEMENT THIS.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onTransduced(TreeEvent<ProximityArchetype> event) {
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
    public ProximityArchetypeTree(ProximityArchetype root) {
	this.model = new Model(new TreePath<ProximityArchetype>(root));
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
     * Inserts the given node into the model.
     * @param node the node to be inserted.
     */
    public void insert(ProximityArchetype node) {
	this.insert(node, false, false);
    }

    /**
     * Inserts the given node into the model.
     * @param node the node to be inserted.
     */
    public void insert(ProximityArchetype node, ProximityArchetype.State state) {
	this.insert(node, state, false, false);
    }

    /**
     * Inserts the given node into the model.
     * @param node the node to be inserted.
     * @param proceed if this value is set to be true, the current path proceeds to the very new node's one.
     */
    public void insert(ProximityArchetype node, boolean proceed) {
	this.insert(node, proceed, false);
    }

    /**
     * Inserts the given node into the model.
     * @param node the node to be inserted.
     * @param state the node state to be inserted.
     * @param proceed if this value is set to be true, the current path proceeds to the very new node's one.
     */
    public void insert(ProximityArchetype node, ProximityArchetype.State state, boolean proceed) {
	this.insert(node, state, proceed, false);
    }

    /**
     * Inserts the given node into the model.
     * @param node the node to be inserted.
     * @param proceed if this value is set to be true, the current path proceeds to the very new node's one.
     * @param mark if this value is set to be true, the previous path is marked.
     */
    public void insert(ProximityArchetype node, boolean proceed, boolean mark) {
	this.insert(this.getModel().getCurrent().getPathTo(node), proceed, mark);
    }

    /**
     * Inserts the given node into the model.
     * @param node the node to be inserted.
     * @param state the node state to be inserted.
     * @param proceed if this value is set to be true, the current path proceeds to the very new node's one.
     * @param mark if this value is set to be true, the previous path is marked.
     */
    public void insert(ProximityArchetype node, ProximityArchetype.State state, boolean proceed, boolean mark) {
	this.insert(this.getModel().getCurrent().getPathTo(node), state, proceed, mark);
    }

    /**
     * Inserts the given path into the model.
     * @param path the node path to be inserted.
     * @param proceed if this value is set to be true, the current path proceeds to the very new node's one.
     * @param mark if this value is set to be true, the previous path is marked.
     */
    public void insert(TreePath<ProximityArchetype> path, boolean proceed, boolean mark) {
	if (mark) {
	    this.getModel().mark();
	}
	this.getModel.put(path);
	this.fireNodesInserted(new TreeEvent<ProximityArchetype>(path.getLastPathElement(), path);)
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
    public void insert(TreePath<ProximityArchetype> path, ProximityArchetype.State state, boolean proceed, boolean mark) {
	if (mark) {
	    this.getModel().mark();
	}
	this.getModel.put(path, state);
	this.fireNodesInserted(new TreeEvent<ProximityArchetype>(path.getLastPathElement(), path));
	if (proceed) {
	    this.getModel().setCurrent(path);
	}
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
	this.getModel().rewind();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fireNodesChanged(TreeEvent event) {
	for (TreeEventListner listener : this.listeners) {
	    listener.onNodesChanged(event);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fireNodesInserted(TreeEvent event) {
	for (TreeEventListner listener : this.listeners) {
	    listener.onNodesInserted(event);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fireNodesRemoved(TreeEvent event) {
	for (TreeEventListner listener : this.listeners) {
	    listener.onNodesRemoved(event);
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
    public ProximityArchetype getChildAt(ProximityArchetype parent, int index) {
	return parent.getChildAt(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getChildCount(ProximityArchetype parent) {
	return parent.getChildCount();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIndexOf(ProximityArchetype parent, ProximityArchetype child) {
	return parent.getIndexOf(child);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProximityArchetype getRoot() {
	return this.root;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isLeaf(ProximityArchetype node) {
	return node.isLeaf();
    }
}
