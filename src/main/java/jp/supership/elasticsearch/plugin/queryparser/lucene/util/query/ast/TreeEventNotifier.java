/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.ast;

/**
 * This interface specifies the implementing class has ability to handle abstract syntax tree
 * related events.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface TreeEventNotifier {
    /**
     * Fires nodes-changed event and hooks registered handlers.
     * @param event the event which will be fired.
     */
    public void fireNodesChanged(TreeEvent event);

    /**
     * Fires nodes-inserted event and hooks registered handlers.
     * @param event the event which will be fired.
     */
    public void fireNodesInserted(TreeEvent event);

    /**
     * Fires nodes-removed event and hooks registered handlers.
     * @param event the event which will be fired.
     */
    public void fireNodesRemoved(TreeEvent event);

    /**
     * Fires tree-transduced event and hooks regisatered handlers.
     * @param event the event which will be fired.
     */
    public void fireTransduced(TreeEvent event);
}
