/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.common.util.function;

import jp.supership.elasticsearch.plugin.queryparser.common.util.ObjectUtils;

/**
 * This abstract class specifies the implementing class can handle the each binary-functional objects.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public abstract class BiFunction<T, U, R> {
    /**
     * Constructor.
     */
    public BiFunction() {
	// DO NOTHING HERE.
    }

    /**
     * Applies this function to the given arguments.
     * @param  first the first function argument.
     * @param  second the second function argument.
     * @return the function result
     */
    public abstract R apply(T first, U second);

    /**
     * Returns a composed function that first applies this function to its input,
     * and then applies the after function to the result.
     * @param  after the function to apply after this function is applied.
     * @return a composed function that first applies this function and then applies the after function.
     * @throws NullPointerException if after is null.
     */
    public <V> BiFunction<T, U, V> andThen(final Function<? super R, ? extends V> after) {
	ObjectUtils.validateNotNull(after);
	return new BiFunction<T, U, V>() {
	    @Override
	    public V apply(T first, U second) {
		return after.apply(BiFunction.this.apply(first, second));
	    }
	};
    }
}
