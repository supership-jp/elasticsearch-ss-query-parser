/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.classic.intermediate;

/**
 * This interface specifies the implementing class must be configured in accordance with the
 * Lucene's {@code QueryParser.Context} API and some additional context.
 *
 * @author Shingo OKAWA
 * @since  08/11/2015
 */
public interface ContextizedQueryParser extends QueryParserContext {
    // This interface is a wrapper-around, just for naming convention.
}
