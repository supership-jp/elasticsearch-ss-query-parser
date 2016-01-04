/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.internal;

/**
 * Translates {@code Token} into an object of type {@code T}, or null if the token is not recvognized.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface TokenTranslator<T> {
    /**
     * Translates {@code Token} into an instance of {@code T}.
     * @param  token the token to be translated.
     * @return the translated object.
     */
    public T translate(Token token);
}
