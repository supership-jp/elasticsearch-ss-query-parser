/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.spans;

import java.util.Map;
import org.apache.lucene.search.spans.SpanQuery;

/**
 * This class is responsible for base implementation of the argumented query classes.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface ProximitySubQueries extends Map<SpanQuery, Float> {
    // THIS IS JUST A WRAPPER.
}
