/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.common.util.function;

import jp.supership.elasticsearch.plugin.queryparser.common.util.ObjectUtils;

/**
 * This interface specifies the implementing class has responsible of consuming result.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public abstract class Consumer<T> {
    /**
     * Conctrutor.
     */
    public Consumer() {
	// DO NOTHING HERE.
    }

    /**
     * Performs this operation on the given argument.
     * @param argument the input argument.
     */
    public abstract void accept(T argument);

    /**
     * Returns a composed {@code Consumer} that performs, in sequence, this operation followed by the after operation.
     * @param  after the operation to perform after this operation.
     * @return a composed {@code Consumer} that performs in sequence this operation followed by the after operation.
     * @throws NullPointerException if after is null.
     */
    public Consumer<T> andThen(final Consumer<? super T> after) {
	ObjectUtils.validateNotNull(after);
	return new Consumer<T>() {
	    @Override
	    public void accept(T argument) {
		Consumer.this.accept(argument);
		after.accept(argument);
	    }
	};
    }
}
