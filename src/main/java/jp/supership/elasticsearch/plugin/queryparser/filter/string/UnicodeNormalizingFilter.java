/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.filter.string;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import jp.supership.elasticsearch.plugin.queryparser.common.util.ConfigUtils;
import jp.supership.elasticsearch.plugin.queryparser.common.util.StringUtils;
import jp.supership.elasticsearch.plugin.queryparser.filter.ChainableFilter;
import jp.supership.elasticsearch.plugin.queryparser.filter.FilterContext;
import jp.supership.elasticsearch.plugin.queryparser.filter.FilterException;
import jp.supership.elasticsearch.plugin.queryparser.filters.Initializable;

/**
 * This {@code ChainableFilter} implementation is responsible for removing excess spaces form
 * the given {@code String}.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class UnicodeNormalizingFilter extends ChainableFilter<String> implements Initializable {
    /** Holds JSON entry key for normalizer form entity. */
    public static final String JSON_NORMALIZER_FORM = "normalizer_form";
    /** Holds normalizer-form definitions. */
    protected static final Map<String, Normalizer.Form> FORMS = new HashMap<String, Normalizer.Form>();

    static {
	FORMS.put("NFC", Normalizer.Form.NFC);
	FORMS.put("NFD", Normalizer.Form.NFD);
	FORMS.put("NFKC", Normalizer.Form.NFKC);
	FORMS.put("NFKD", Normalizer.Form.NFKD);
    }

    /** Holds the regular expression pattern to be removed. */
    private Normalizer.Form normalizerForm = Normalizer.Form.NFKC;

    /**
     * Constructor.
     */
    public UnicodeNormalizingFilter() {
	// DO NOTHING.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(Map<String, Object> settings) throws IllegalArgumentException {
	String formName = ConfigUtils.getStringValue(settings, JSON_NORMALIZER_FORM);
	if (!StringUtils.isEmpty(formName) && FORMS.containsKey(formName)) {
	    this.normalizerForm = FORMS.get(formName);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String doFilter(String target, FilterContext<String, Object> context) throws FilterException {
	return Normalizer.normalize(target, this.normalizerForm);
    }
}
