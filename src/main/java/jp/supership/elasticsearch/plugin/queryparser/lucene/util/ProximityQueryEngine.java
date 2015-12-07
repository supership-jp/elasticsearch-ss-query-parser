/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util;

import java.util.List;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanQuery;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.config.DSQParserConfiguration;

/**
 * This class is responsible for instanciating Lucene queries, query parser delegates all sub-query
 * instanciation tasks to this class. This implementation assumes that concrete query handler will be 
 * constructed in accordance to the given ANTLR grammar.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public abstract class ProximityQueryEngine extends QueryEngine implements ProximityQueryDriver {
    /**
     * Constructor.
     */
    public ProximityQueryEngine() {
        super();
    }

    /**
     * Constructor.
     */
    public ProximityQueryEngine(DSQParserConfiguration configuration) {
        super();
        this.configuration = configuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void conjugate(List<SpanQuery> clauses, int conjunction, int modifier, int slop, boolean inOrder, SpanQuery query) {
    }
}
