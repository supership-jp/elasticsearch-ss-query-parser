/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.internal;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import jp.supership.elasticsearch.plugin.queryparser.common.util.function.Function;
import jp.supership.elasticsearch.plugin.queryparser.common.util.function.Predicate;
import jp.supership.elasticsearch.plugin.queryparser.common.util.function.Supplier;

/**
 * Represents atomic parser entities, i.e., this classes instances defines grammar and encapsulates parsing
 * logic. A {@code Parser} takes as input a {@code CharSequence} source and parses it when the {@code #parse(CharSequence)}
 * method is called.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public abstract class Parser<T> {
    /**
     * Represents atomic mutable reference to {@code Parser} used in recursive grammars.
     */
    @SuppressWarnings("serial")
    public static final class Reference<T> extends AtomicReference<Parser<T>> {
	//	/** Holds instance which will be utilized for lazy evaluation. */
	//	private final Parser<T> lazy = new Parser<T>() {
	//	    /** {@inheritDoc} */
	//	    @Override
	//	    public boolean apply() {
	//	    }
	//	};
    }
}
