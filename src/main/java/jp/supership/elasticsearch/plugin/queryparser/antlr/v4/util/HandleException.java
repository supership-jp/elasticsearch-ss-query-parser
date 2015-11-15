/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util;

/**
 * The exception thrown when an error occures while analyzing query.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public final class HandleException extends RuntimeException {
    /**
     * Constructs a new handler exception with the specified message.
     * @param message the exception detail message.
     */
    public HandleException(String message) {
	super(message);
    }

    /**
     * Constructs a new handler exception with the specified cause.
     * @param cause the causial exception to be passed.
     */
    public HandleException(Throwable cause) {
	super(cause);
    }

    /**
     * Constructs a new handler exception with the specified message and cause.
     * @param message the exception detail message.
     * @param cause the causial exception to be passde.
     */
    public HandleException(String message, Throwable cause) {
	super(message, cause);
    }
}
