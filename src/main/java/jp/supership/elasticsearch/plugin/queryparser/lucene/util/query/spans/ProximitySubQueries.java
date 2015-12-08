/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.spans;

import jp.supership.elasticsearch.plugin.queryparser.lucene.util.ProximityQueryDriver;

/**
 * This class is responsible for base implementation of the argumented query classes.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface ProximitySubQueries {
    /** When distanceSubQueryNotAllowed() returns non null, the reason why the subquery
     * is not allowed as a distance subquery is returned.
     * <br>When distanceSubQueryNotAllowed() returns null addSpanNearQueries() can be used
     * in the creation of the span near clause for the subquery.
     */
    String distanceSubQueryNotAllowed();
    
    void addSpanQueries(ProximityQueryDriver driver);
}
