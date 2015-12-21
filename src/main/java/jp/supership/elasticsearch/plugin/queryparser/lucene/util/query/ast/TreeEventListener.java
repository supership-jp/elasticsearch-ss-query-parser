/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.ast;

import java.util.EventListener;

/**
 * This interface specifies the implementing class has ability to handle abstract syntax tree
 * related events.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface TreeEventListener<N extends Node> extends EventListener {
    /**
     * This method will be hooked when the handling nodes were changed.
     * @param event the target event which will be handled.
     */
    public void onNodesChanged(TreeEvent<N> event);

    /**
     * This method will be hooked when the new nodes were inserted.
     * @param event the target event which will be handled.
     */
    public void onNodesInserted(TreeEvent<N> event);

    /**
     * This method will be hooked when the some nodes were removed.
     * @param event the target event which will be handled.
     */
    public void onNodesRemoved(TreeEvent<N> event);

    /**
     * This method will be hooked when the tree path ascended.
     * @param event the target event which will be handled.
     */
    public void onPathAscended(TreeEvent<N> event);

    /**
     * This method will be hooked when the tree path changed.
     * @param event the target event which will be handled.
     */
    public void onPathDescended(TreeEvent<N> event);

    /**
     * This method will be hooked when the structure of the tree was changed.
     * @param event the target event which will be handled.
     */
    public void onTransduced(TreeEvent<N> event);
}
