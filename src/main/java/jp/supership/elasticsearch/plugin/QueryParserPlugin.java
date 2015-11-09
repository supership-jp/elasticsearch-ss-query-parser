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
 * @since  04/11/2015
 */
public class QueryParserPlugin extends AbstractPlugin {
    /**
     * @inheritdoc
     */
    @Override
    public String name() {  
        return "supership query parser";  
    }  

    /**
     * @inheritdoc
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
