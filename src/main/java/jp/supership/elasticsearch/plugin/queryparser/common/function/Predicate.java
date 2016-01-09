/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.common.util.function;

import jp.supership.elasticsearch.plugin.queryparser.common.util.ObjectUtils;

/**
 * This interface specifies the implementing class can handle the each prediction objects.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public abstract class Predicate<T> {
    /**
     * Returns a predicate that tests if two arguments are equal according to {@link Objects#equals}.
     * @param  <T> the type of arguments to the predicate.
     * @param  target the object reference with which to compare for equality, which may be null.
     * @return a predicate that tests if two arguments are equal according to {@link Objects#equals}.
     */
    static <T> Predicate<T> isEqual(final Object target) {
	if (target == null) {
	    return new Predicate<T>() {
		@Override
		public boolean test(T argument) {
		    return ObjectUtils.isNull(argument);
		}
	    };
	} else {
	    return new Predicate<T>() {
		@Override
		public boolean test(T argument) {
		    return target.equals(argument);
		}
	    };
	}
    }

    /**
     * Constructor.
     */
    public Predicate() {
	// DO NOTHING HERE.
    }

    /**
     * Evaluates this predicate on the given argument.
     * @param  argument the input argument.
     * @return true if the input argument matches the predicate, otherwise false.
     */
    public abstract boolean test(T argument);

    /**
     * Returns a composed predicate that represents a short-circuiting logical AND of this predicate and another.
     * @param  other a predicate that will be logically-ANDed with this predicate.
     * @return a composed predicate that represents the short-circuiting logical AND of this predicate and the {@code other} predicate.
     * @throws NullPointerException if other is null.
     */
    public Predicate<T> and(final Predicate<? super T> other) {
	ObjectUtils.validateNotNull(other);
	return new Predicate<T>() {
	    @Override
	    public boolean test(T argument) {
		return Predicate.this.test(argument) && other.test(argument);
	    }
	};
    }

    /**
     * Returns a predicate that represents the logical negation of this predicate.
     * @return a predicate that represents the logical negation of this predicate.
     */
    public Predicate<T> negate() {
	return new Predicate<T>() {
	    @Override
	    public boolean test(T argument) {
		return !Predicate.this.test(argument);
	    }
	};
    }

    /**
     * Returns a composed predicate that represents a short-circuiting logical OR of this predicate and another.
     * @param  other a predicate that will be logically-ORed with this predicate.
     * @return a composed predicate that represents the short-circuiting logical OR of this predicate and the {@code other} predicate.
     * @throws NullPointerException if other is null.
     */
    public Predicate<T> or(final Predicate<? super T> other) {
	ObjectUtils.validateNotNull(other);
	return new Predicate<T>() {
	    @Override
	    public boolean test(T argument) {
		return Predicate.this.test(argument) || other.test(argument);
	    }
	};
    }
}

