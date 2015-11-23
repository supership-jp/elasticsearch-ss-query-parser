/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.filter;

/**
 * The exception thrown when an error occures while filtering processes is running.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public final class FilterException extends RuntimeException {
    /**
     * Constructs a new filter exception with the specified message.
     * @param message the exception detail message.
     */
    public FilterException(String message) {
	super(message);
    }

    /**
     * Constructs a new filter exception with the specified cause.
     * @param cause the causial exception to be passed.
     */
    public FilterException(Throwable cause) {
	super(cause);
    }

    /**
     * Constructs a new filter exception with the specified message and cause.
     * @param message the exception detail message.
     * @param cause the causial exception to be passde.
     */
    public FilterException(String message, Throwable cause) {
	super(message, cause);
    }
}
