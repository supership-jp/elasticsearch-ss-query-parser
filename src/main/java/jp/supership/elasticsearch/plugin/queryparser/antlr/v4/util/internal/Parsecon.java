/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.internal;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents atomic parser entities, i.e., this classes instances defines grammar and encapsulates parsing
 * logic. A {@code Parsecon} takes as input a {@code CharSequence} source and parses it when the {@code #parse(CharSequence)}
 * method is called.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public abstract class Parsecon<T> {
    /**
     * Represents atomic mutable reference to {@code Parsecon} used in recursive grammars.
     */
    @SuppressWarnings("serial")
    public static final class Reference<T> extends AtomicReference<Parsecon<T>> {
    }
}
