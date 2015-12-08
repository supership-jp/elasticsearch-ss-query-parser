/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.spans;

import java.util.Collection;
import org.apache.lucene.search.Query;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.ProximityQueryDriver;

/**
 * This class is responsible for base implementation of the argumented query classes.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public abstract class ProximityQuery extends QueryComposition {
    /** Holds slop value. */
    private int slop;

    /** Sets to be true if the queries must be ordered. */
    private boolean ordered;

    /**
     * Constructor.
     */
    public ProximityQuery(Collection<ArgumentedQuery> queries, boolean infixed, int slop, int operator, boolean ordered) {
	super(queries, infixed, operator);
	this.slop = slop;
	this.ordered = ordered;
    }
}
