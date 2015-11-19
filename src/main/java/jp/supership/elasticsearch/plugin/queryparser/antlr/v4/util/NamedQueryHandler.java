/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util;

import jp.supership.elasticsearch.plugin.queryparser.handlers.QueryHandlerWrapper;

/**
 * This interface specifies the implementing class can handle the given {@code java.io.Reader}
 * as raw query string and instanciates {@code org.apache.lucene.search.Query}.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface NamedQueryHandler extends QueryHandler {
    /**
     * Returns the assigned name of the {@code QueryHandler}.
     */
    public String getName();
}
