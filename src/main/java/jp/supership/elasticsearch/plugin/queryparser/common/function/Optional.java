/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.common.util.function;

import java.util.NoSuchElementException;
import jp.supership.elasticsearch.plugin.queryparser.common.util.ObjectUtils;

/**
 * A container object which may or may not contain a non-null value.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public final class Optional<T> {
    /** Holds singleton instance for {@code empty()}. */
    private static final Optional<?> EMPTY = new Optional<>();

    /**
     * Returns an empty {@code Optional} instance.
     * @return an empty {@code Optional}.
     */
    public static <T> Optional<T> empty() {
	@SuppressWarnings("unchecked")
	Optional<T> result = (Optional<T>) EMPTY;
	return result;
    }

    /**
     * Returns an {@code Optional} with the specified present non-null value.
     * @param  value the value to be present, which must be non-null.
     * @return an {@code Optional} with the value present.
     * @throws NullPointerException if value is null.
     */
    public static <T> Optional<T> of(T value) {
	return new Optional<>(value);
    }

    /**
     * Returns an {@code Optional} describing the specified value, if non-null, otherwise returns an empty {@code Optional}.
     * @param  value the possibly-null value to describe
     * @return an {@code Optional} with a present value if the specified value is non-null, otherwise an empty {@code Optional}
     */
    public static <T> Optional<T> ofNullable(T value) {
	return value == null ? Optional.<T>empty() : Optional.<T>of(value);
    }

    /** Holds assigned value. If non-null, the value; if null, indicates no value is present. */
    private final T value;

    /**
     * Constructor.
     */
    private Optional() {
	this.value = null;
    }

    /**
     * Constructor.
     */
    private Optional(T value) {
	this.value = (T) ObjectUtils.ensureNotNull(value);
    }

    /**
     * If a value is present in this {@code Optional}, returns the value, otherwise throws {@code NoSuchElementException}.
     * @return the non-null value held by this {@code Optional}.
     * @throws NoSuchElementException if there is no value present.
     */
    public T get() {
	if (this.value == null) {
	    throw new NoSuchElementException("no value present.");
	}
	return this.value;
    }

    /**
     * Return true if there is a value present, otherwise false.
     * @return true if there is a value present, otherwise false.
     */
    public boolean isPresent() {
	return this.value != null;
    }

    /**
     * If a value is present, invoke the specified consumer with the value, otherwise do nothing.
     * @param  consumer block to be executed if a value is present.
     * @throws NullPointerException if value is present and {@code consumer} is null.
     */
    public void ifPresent(Consumer<? super T> consumer) {
	if (this.value != null)
	    consumer.accept(value);
    }

    /**
     * If a value is present, and the value matches the given predicate, return an {@code Optional} describing the value,
     * otherwise return an empty {@code Optional}.
     * @param  predicate a predicate to apply to the value, if present.
     * @return an {@code Optional} describing the value of this {@code Optional}.
     * @throws NullPointerException if the predicate is null.
     */
    public Optional<T> filter(Predicate<? super T> predicate) {
	ObjectUtils.validateNotNull(predicate);
	if (!this.isPresent()) {
	    return this;
	} else {
	    return predicate.test(value) ? this : Optional.<T>empty();
	}
    }

    /**
     * If a value is present, apply the provided mapping function to it, and if the result is non-null,
     * return an {@code Optional} describing the result.  Otherwise return an empty {@code Optional}.
     * @param  mapper a mapping function to apply to the value, if present.
     * @return an {@code Optional} describing the result of applying a mapping function to the value of this {@code Optional}.
     * @throws NullPointerException if the mapping function is null.
     */
    public<U> Optional<U> map(Function<? super T, ? extends U> mapper) {
	ObjectUtils.validateNotNull(mapper);
	if (!this.isPresent()) {
	    return Optional.empty();
	} else {
	    return Optional.ofNullable(mapper.apply(value));
	}
    }

    /**
     * If a value is present, apply the provided {@code Optional}-bearing mapping function to it,
     * return that result, otherwise return an empty {@code Optional}.
     * @param  mapper a mapping function to apply to the value, if present the mapping function.
     * @return the result of applying an {@code Optional}-bearing mapping function to the value of this {@code Optional}.
     * @throws NullPointerException if the mapping function is null or returns a null result.
     */
    public<U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper) {
	ObjectUtils.validateNotNull(mapper);
	if (!this.isPresent()) {
	    return Optional.empty();
	} else {
	    return (Optional<U>) ObjectUtils.ensureNotNull(mapper.apply(value));
	}
    }

    /**
     * Return the value if present, otherwise return other.
     * @param  other the value to be returned if there is no value present, may be null.
     * @return the value, if present, otherwise other.
     */
    public T orElse(T other) {
	return this.value != null ? this.value : other;
    }

    /**
     * Return the value if present, otherwise invoke other and return the result of that invocation.
     * @param  other a {@code Supplier} whose result is returned if no value is present.
     * @return the value if present otherwise the result of {@code other#get()}.
     * @throws NullPointerException if value is not present and {@code other} is null.
     */
    public T orElseGet(Supplier<? extends T> other) {
	return this.value != null ? this.value : other.get();
    }

    /**
     * Return the contained value, if present, otherwise throw an exception to be created by the provided supplier.
     * @param  other the supplier which will return the exception to be thrown.
     * @return the present value.
     * @throws X if there is no value present.
     * @throws NullPointerException if no value is present and {@code other} is null.
     */
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> other) throws X {
	if (this.value != null) {
	    return this.value;
	} else {
	    throw other.get();
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object object) {
	if (this == object) {
	    return true;
	}

	if (!(object instanceof Optional)) {
	    return false;
	}

	Optional<?> that = (Optional<?>) object;
	return ObjectUtils.equals(this.value, that.value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
	return ObjectUtils.hashCode(this.value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
	return this.value != null ? String.format("Optional[%s]", this.value) : "Optional.empty";
    }
}
