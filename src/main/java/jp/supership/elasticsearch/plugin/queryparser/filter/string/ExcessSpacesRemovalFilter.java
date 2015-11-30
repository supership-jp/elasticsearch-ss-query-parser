/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.filter.string;

import jp.supership.elasticsearch.plugin.queryparser.filter.Chainable;
import jp.supership.elasticsearch.plugin.queryparser.filter.ChainContext;
import jp.supership.elasticsearch.plugin.queryparser.filter.FilterException;

/**
 * This interface specifies the implementing class has ability to handle chained process.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class ExcessSpacesRemovalFilter extends Chainable<String> {
    /** Holds the regular expression pattern to be removed. */
    private static final String SPACES_REGEXP = "\\s+";

    /** Holds the string which will take palce for the spaces. */
    private static final String SUBSTITUTION = " ";

    /**
     * Constructor.
     */
    public ExcessSpacesRemovalFilter() {
	// DO NOTHING.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doChain(String chained, ChainContext<String, Object> chainContext) throws FilterException {
	chained = chained.replaceAll(SPACES_REGEXP, SUBSTITUTION);
    }
}
