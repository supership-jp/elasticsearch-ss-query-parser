/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.lucene.util.query.ast;

import java.util.List;

/**
 * This interface specifies the implementing class has ability to transduce trees.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface TreeTransducer<S extends Tree, T extends Tree> {
    /**
     * Returns the index of node in the parent children.
     * @param  parent the target parent.
     * @param  child the target child.
     * @return the index of the given child.
     */
    public T transduce(S source);
}
