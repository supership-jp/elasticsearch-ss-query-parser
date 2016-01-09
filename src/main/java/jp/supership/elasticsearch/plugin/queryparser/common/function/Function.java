/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.common.util.function;

import jp.supership.elasticsearch.plugin.queryparser.common.util.ObjectUtils;

/**
 * This abstract class specifies the implementing class can handle the each functional objects.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public abstract class Function<T, R> {
    /**
     * Returns a function that always returns its input argument.
     * @return a identity function.
     */
    static <T> Function<T, T> identity() {
	return new Function<T, T>() {
	    @Override
	    public T apply(T argument) {
		return argument;
	    }
	};
    }

    /**
     * Constructor.
     */
    public Function() {
	// DO NOTHING HERE.
    }

    /**
     * Applies this function to the given argument.
     * @param  argument the function argument.
     * @return the function result
     */
    public abstract R apply(T argument);

    /**
     * Returns a composed function that first applies the before function to its input,
     * and then applies this function to the result.
     * @param  before the function to apply before this function is applied.
     * @return a composed function that first applies the before function and then applies this function.
     * @throws NullPointerException if before is null.
     */
    public <V> Function<V, R> compose(final Function<? super V, ? extends T> before) {
	ObjectUtils.validateNotNull(before);
	return new Function<V, R>() {
	    @Override
	    public R apply(V argument) {
		return Function.this.apply(before.apply(argument));
	    }
	};
    }

    /**
     * Returns a composed function that first applies this function to its input,
     * and then applies the after function to the result.
     * @param  after the function to apply after this function is applied.
     * @return a composed function that first applies this function and then applies the after function.
     * @throws NullPointerException if after is null.
     */
    public <V> Function<T, V> andThen(final Function<? super R, ? extends V> after) {
	ObjectUtils.validateNotNull(after);
	return new Function<T, V>() {
	    @Override
	    public V apply(T argument) {
		return after.apply(Function.this.apply(argument));
	    }
	};
    }
}
