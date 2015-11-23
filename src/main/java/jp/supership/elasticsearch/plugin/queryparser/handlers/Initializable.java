/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.handlers;

/**
 * This interface specifies that the implementing class has ability to be registered
 * within {@code QueryHandlerFactory} registry.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface Initializable {
    /**
     * Hooks some initialization protocols.
     */
    public void initialize(QueryHandlerFactory.Arguments arguments);
}
