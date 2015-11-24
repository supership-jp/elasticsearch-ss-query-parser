/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.handlers;

import java.util.Map;
import java.util.HashMap;
import jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util.QueryHandler;
import jp.supership.elasticsearch.plugin.queryparser.lucene.util.config.QueryEngineConfiguration;
import jp.supership.elasticsearch.plugin.queryparser.util.ConfigUtils;
import jp.supership.elasticsearch.plugin.queryparser.util.StringUtils;

/**
 * This class is responsible for instanciating named {@code QueryHandler}.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public class NamedQueryHandlerFactory implements QueryHandlerFactory<String> {
    /** Holds JSON entry key for settings entity. */
    public static final String JSON_SETTINGS = "settings";

    /** Holds JSON entry key for class entity. */
    public static final String JSON_CLASS = "class";

    /** Holds JSON entry key for name entity. */
    public static final String JSON_NAME = "name";

    /** Holds map between {@code String} keys and {@code QueryHandler} insatnces. */
    private static final Map<String, Object> JSON;

    static {
	try {
	    JSON = (Map<String, Object>) (ConfigUtils.loadJSONFromClasspath("/resources/NamedQueryHandlerFactory.json"));
	} catch (Exception cause) {
	    throw new ExceptionInInitializerError(cause);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryHandler create(String key, QueryHandlerFactory.Arguments arguments) throws IllegalArgumentException {
	return this.create(key, JSON, arguments);
    }

    /**
     * Returns wrapped {@code QueryHandler} instance.
     * @param  key the key which is associated with the requesting {@code QueryHandler}.
     * @param  configuration the configuration which will be used for instanciating {@code QueryHandler}.
     * @return the wrapped {@code QueryHandler} instance.
     */
    private QueryHandler create(String key, Map<String, Object> json, QueryHandlerFactory.Arguments arguments) throws IllegalArgumentException {
	Object entry = json.get(key);
	if (entry != null && !(entry instanceof Map)) {
	    throw new IllegalArgumentException("entry must be Map for handler " + key);
	}

	Map<String, Object> configuration = (Map<String, Object>) entry;

	String clazz = ConfigUtils.getStringValue(configuration, JSON_CLASS);
	if (StringUtils.isEmpty(clazz)) {
	    throw new IllegalArgumentException("'class' element not defined for handler " + key);
	}

	Object settings = configuration.get(JSON_SETTINGS);
	if (settings != null && !(settings instanceof Map)) {
	    throw new IllegalArgumentException("'settings' element must be Map for handler " + key);
	}

	try {
	    Initializable handler = (Initializable) Class.forName(clazz).newInstance();
	    handler.initialize(arguments);
	    return (QueryHandler) handler;
	} catch (InstantiationException cause) {
	    throw new IllegalArgumentException(clazz + " creation exception " + cause.getMessage(), cause);
	} catch (IllegalAccessException cause) {
	    throw new IllegalArgumentException(clazz + " creation exception " + cause.getMessage(), cause);
	} catch (ClassNotFoundException cause) {
	    throw new IllegalArgumentException(clazz + " not found", cause);
	} catch (ClassCastException cause) {
	    throw new IllegalArgumentException(clazz + " must implement interface " + QueryHandler.class.getName());
	}
    }
}
