/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.common.util;

/**
 * A collection of operations that relates to objects, i.e., {@code Object} instances.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public final class ObjectUtils {
    /**
     * Returns the hashcode for {@code Object}. 0 is returned if the object is null.
     * @param  object the object to be hashed.
     * @return the hashcode for the given object.
     */
    public static int hashCode(Object object) {
	return object == null ? 0 : object.hashCode();
    }

    /**
     * Compares the given two objects for equality. Returns true if the given objects are considered to
     * be equal.
     * @param  left the comparetee.
     * @param  right the comparetee.
     * @return true if the given objects are considered to be equal.
     */
    public static boolean equals(Object left, Object right) {
	return left == null ? right == null : left.equals(right);
    }

    /**
     * Checks if the given {@code Object} belongs to the given {@code Array}.
     * @param object the object to be checked.
     * @param array the array to be checked.
     * @return true if the given object belongs to the given array.
     */
    public static boolean in(Object object, Object... array) {
	for (Object candidate : array) {
	    if (object == candidate) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Returns true if the provided reference is null otherwise returns false.
     * @param  object a reference to be checked against null.
     * @return true if the provided reference is null otherwise false.
     */
    public static boolean isNull(Object object) {
	return object == null;
    }

    /**
     * Validates that the given value is not null.
     * @param value the value to be checked.
     */
    public static void validateNotNull(Object value) throws NullPointerException {
	if (value == null) {
	    throw new NullPointerException();
	}
    }

    /**
     * Validates that the given condition stands true.
     * @param condition the condition to be checked.
     * @param message the error message.
     */
    public static void validateCondition(boolean condition, String message) throws IllegalArgumentException {
	if (!condition) {
	    throw new IllegalArgumentException(message);
	}
    }

    /**
     * Validates that the given condition stands true.
     * @param condition the condition to be checked.
     * @param format the error message format.
     * @param arguments the error message arguments.
     */
    public static void validateCondition(boolean condition, String format, Object... arguments) throws IllegalArgumentException {
	if (!condition) {
	    throw new IllegalArgumentException(String.format(format, arguments));
	}
    }
}
