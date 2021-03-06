/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.internal;

import jp.supership.elasticsearch.plugin.queryparser.common.util.ObjectUtils;

/**
 * Represents any token with a token value and the 0-based index in the source.
 * This class is responsible to handle key-value pairs which will be generated by the ANTLR.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public final class Token {
    /** Holds the index of the appearence in the source code. */
    private final int index;
    /** Holds the length of the token. */
    private final int length;
    /** Holds the value of the token. */
    private final Object value;

    /**
     * Constructor.
     */
    public Token(int index, int length, Object value) {
	this.index = index;
	this.length = length;
	this.value = value;
    }

    /**
     * Returns the index of the token.
     * @return the index of the token.
     */
    public int getIndex() {
	return this.index;
    }

    /**
     * Returns the length of the token.
     * @return the length of the token.
     */
    public int getLength() {
	return this.length;
    }

    /**
     * Returns the value of the token.
     * @return the value of the token.
     */
    public Object getValue() {
	return this.value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
	return String.valueOf(this.value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
	return (this.index * 31 + this.length) * 31 + ObjectUtils.hashCode(this.value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object object) {
	if (object instanceof Token) {
	    Token that = (Token) object;
	    return this.index == that.index && this.length == that.length && ObjectUtils.equals(this.value, that.value);
	}
	return false;
    }
}
