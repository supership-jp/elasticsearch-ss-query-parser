/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.filters;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import jp.supership.elasticsearch.plugin.queryparser.common.util.ConfigUtils;
import jp.supership.elasticsearch.plugin.queryparser.common.util.StringUtils;
import jp.supership.elasticsearch.plugin.queryparser.filter.ChainableFilter;

/**
 * This class is responsible for instanciating named {@code ChainableFilter<String>}.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class NamedStringFilterChainFactory implements FilterChainFactory<String, String> {
    /** Holds JSON entry key for settings entity. */
    public static final String JSON_SETTINGS = "settings";

    /** Holds JSON entry key for class entity. */
    public static final String JSON_CLASS = "class";

    /** Holds JSON entry key for name entity. */
    public static final String JSON_NAME = "name";

    /** Holds map between {@code String} keys and {@code ChainableFilter} insatnces. */
    private static final Map<String, Object> JSON;

    static {
	try {
	    JSON = ConfigUtils.loadJSONFromClasspath("/NamedStringFilterChainFactory.json");
	} catch (Exception cause) {
	    throw new ExceptionInInitializerError(cause);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChainableFilter<String> create() throws IllegalArgumentException {
	return this.create(null, JSON);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChainableFilter<String> create(String key) throws IllegalArgumentException {
	return this.create(key, JSON);
    }

    /**
     * Returns chained {@code ChainableFilter} instance.
     * @param  key the key string which is associated with the each {@code ChanableFilter}.
     * @param  json the configuration which will be used for instanciating {@code ChanableFilter}.
     * @return the chained {@code ChainableFilter} instance.
     */
    private ChainableFilter<String> create(String key, Map<String, Object> json) throws IllegalArgumentException {
	ChainableFilter<String> result = null;
	ChainableFilter<String> current = null;
	Set<String> keys = json.keySet();

	for (String currentKey : keys) {
	    if (key == null || currentKey.startsWith(key)) {
		Object entry = json.get(currentKey);
		if (entry != null && !(entry instanceof Map)) {
		    throw new IllegalArgumentException("entry must be Map for filter " + key);
		}
		Map<String, Object> configuration = (Map<String, Object>) entry;

		String clazz = ConfigUtils.getStringValue(configuration, JSON_CLASS);
		if (StringUtils.isEmpty(clazz)) {
		    throw new IllegalArgumentException("'class' element not defined for filter " + key);
		}

		Object map = configuration.get(JSON_SETTINGS);
		if (map != null && !(map instanceof Map)) {
		    throw new IllegalArgumentException("'settings' element must be Map for field " + key);
		}
		Map<String, Object> settings = (Map<String, Object>) map;

		try {
		    Initializable filter = (Initializable) Class.forName(clazz).newInstance();
		    filter.initialize(settings);
		    if (result == null) {
			result = current = (ChainableFilter<String>) filter;
		    } else {
			current.setNext((ChainableFilter<String>) filter);
			current = (ChainableFilter<String>) filter;
		    }
		} catch (InstantiationException cause) {
		    throw new IllegalArgumentException(clazz + " creation exception " + cause.getMessage(), cause);
		} catch (IllegalAccessException cause) {
		    throw new IllegalArgumentException(clazz + " creation exception " + cause.getMessage(), cause);
		} catch (ClassNotFoundException cause) {
		    throw new IllegalArgumentException(clazz + " not found", cause);
		} catch (ClassCastException cause) {
		    throw new IllegalArgumentException(clazz + " must implement interface " + ChainableFilter.class.getName());
		}
	    }
	}

	return result;
    }
}
