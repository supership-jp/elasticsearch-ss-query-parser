/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin;

import org.elasticsearch.index.query.IndexQueryParserModule;
import org.elasticsearch.plugins.AbstractPlugin;
import jp.supership.elasticsearch.plugin.queryparser.DSQParser;

/**
 * A general-purpose query parser implementation for Supership alianses.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class QueryParserPlugin extends AbstractPlugin {
    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {  
        return "supership query parser";  
    }  

    /**
     * {@inheritDoc}
     */
    @Override
    public String description() {
        return "supership query parser plugin";
    }

    /**
     * A plugin can dynamically injected with {@link Module} by implementing <tt>onMojule(AnyModule)</tt>
     */
    public void onModule(IndexQueryParserModule module){
        module.addQueryParser("ss_query_parser", DSQParser.class);
    }
}
