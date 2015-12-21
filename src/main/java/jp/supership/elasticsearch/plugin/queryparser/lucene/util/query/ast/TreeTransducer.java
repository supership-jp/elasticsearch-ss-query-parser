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
     * Transduces the given tree into the other tree.
     * @param  source the source tree to be transduced.
     * @return the transduced tree.
     */
    public T transduce(S source);
}
