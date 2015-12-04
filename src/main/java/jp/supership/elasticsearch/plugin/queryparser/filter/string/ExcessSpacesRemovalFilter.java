/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.filter.string;

import java.util.Map;
import jp.supership.elasticsearch.plugin.queryparser.common.util.ConfigUtils;
import jp.supership.elasticsearch.plugin.queryparser.common.util.StringUtils;
import jp.supership.elasticsearch.plugin.queryparser.filter.ChainableFilter;
import jp.supership.elasticsearch.plugin.queryparser.filter.FilterContext;
import jp.supership.elasticsearch.plugin.queryparser.filter.FilterException;
import jp.supership.elasticsearch.plugin.queryparser.filters.Initializable;

/**
 * This {@code ChainableFilter} implementation is responsiblefor removing excessing spaces from
 * the given string.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class ExcessSpacesRemovalFilter extends ChainableFilter<String> implements Initializable {
    /** Holds JSON entry key for removal pattern entity. */
    public static final String JSON_REMOVAL_PATTERN = "removal_pattern";

    /** Holds JSON entry key for substitution entity. */
    public static final String JSON_SUBSTITUTION = "substitution";

    /** Holds the regular expression pattern to be removed. */
    private String removalRegexp;

    /** Holds the string which will take palce for the spaces. */
    private String substitution;

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
    public void initialize(Map<String, Object> settings) throws IllegalArgumentException {
	this.removalRegexp = ConfigUtils.getStringValue(settings, JSON_REMOVAL_PATTERN);
	if (StringUtils.isEmpty(this.removalRegexp)) {
	    throw new IllegalArgumentException("'removal_pattern' element not defined for filter " + ExcessSpacesRemovalFilter.class.getName());
	}
	this.substitution = ConfigUtils.getStringValue(settings, JSON_SUBSTITUTION);
	if (this.substitution == null) {
	    throw new IllegalArgumentException("'substitution' element not defined for filter " + ExcessSpacesRemovalFilter.class.getName());
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String doFilter(String target, FilterContext<String, Object> context) throws FilterException {
	return target.replaceAll(this.removalRegexp, this.substitution);
    }
}
