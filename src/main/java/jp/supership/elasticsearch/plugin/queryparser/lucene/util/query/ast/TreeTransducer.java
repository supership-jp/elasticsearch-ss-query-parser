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
public abstract class TreeTransducer<S extends Tree, N extends Node, T extends Tree> extends TreeVisitor<N> {
    /**
     * Transduces the given tree into the other tree.
     * @param  source the source tree to be transduced.
     * @return the transduced tree.
     */
    public abstract T transduce(S source);
}
