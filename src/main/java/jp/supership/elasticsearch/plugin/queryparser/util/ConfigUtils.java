/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.common.settings.SettingsException;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;

/**
 * A collection of operations that relates to configurations, i.e., {@code Map} instances.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public final class ConfigUtils {
    /**
     * Typesafe get value from map as {@link Integer} object instance if possible.
     * @param  values to get value from.
     * @param  key to get value from Map.
     * @return Integer value or null.
     * @throws NumberFormatException if value can't be converted to the int value
     * 
     */
    public static Integer getIntegerValue(Map<String, Object> values, String key) throws NumberFormatException {
        if (StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException("key must be defined");
        }

        if (values == null) {
            return null;
        }

        Object node = values.get(key);
        if (node == null) {
            return null;
        }

        if (node instanceof Integer) {
            return (Integer) node;
        } else if (node instanceof Number) {
            return new Integer(((Number) node).intValue());
        }

        return Integer.parseInt(node.toString());
    }

    /**
     * Typesafe get value from map as {@link String}. An {@link Object#toString()} is used for nonstring objects.
     * @param  values to get value from.
     * @param  key to get value from Map. Must be defined.
     * @return value for given key as String.
     */
    public static String getStringValue(Map<String, Object> values, String key) {
        if (StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException("key must be defined");
        }

        if (values == null) {
            return null;
        }

        Object node = values.get(key);
        if (node == null) {
            return null;
        } else {
            return node.toString();
        }
    }

    /**
     * Typesafe get value from map as {@link List} of {@link String}.
     * @param  values to get value from.
     * @param  key to get value from Map.
     * @return value for given key as List of String. Never empty, null is returned in this cases.
     */
    @SuppressWarnings("unchecked")
    public static List<String> getStringValues(Map<String, Object> values, String key) {
        if (StringUtils.isEmpty(key)) {
            throw new IllegalArgumentException("key must be defined");
        }

        if (values == null) {
            return null;
        }

        Object node = values.get(key);
        if (node == null) {
            return null;
        } else if (node instanceof List) {
            List<Object> nodes = (List<Object>) node;
            if (nodes.isEmpty()) {
                return null;
            }
            List<String> result = new ArrayList<String>();
            for (Object item : nodes) {
                if (item != null) {
                    String s = StringUtils.trimToNull(item.toString());
                    if (s != null) {
                        result.add(s);
                    }
                }
            }
            if (result.isEmpty()) {
                return null;
            } else {
                return result;
            }
        } else if (node instanceof Map) {
            return null;
        } else {
            if (node instanceof String) {
                node = StringUtils.trimToNull((String) node);
                if (node == null) {
                    return null;
                }
            }
            List<String> result = new ArrayList<String>();
            result.add(node.toString());
            return result;
        }
    }

    /**
     * Read file from classpath into String. UTF-8 encoding expected.
     * @param  path in classpath to read data from.
     * @return file content.
     * @throws IOException
     */
    public static String readStringFromClasspath(String path) throws IOException {
	StringWriter stringWriter = new StringWriter();
	IOUtils.copy(ConfigUtils.class.getResourceAsStream(path), stringWriter, "UTF-8");
	return stringWriter.toString();
    }

    /**
     * Read JSON file from classpath into Map of Map structure.
     * @param  path path in classpath pointing to JSON file to read
     * @return parsed JSON file
     * @throws SettingsException
     */
    public static Map<String, Object> loadJSONFromClasspath(String path) throws IOException {
	XContentParser parser = null;
	try {
	    parser = XContentFactory.xContent(XContentType.JSON).createParser(ConfigUtils.class.getResourceAsStream(path));
	    return parser.mapAndClose();
	} finally {
	    if (parser != null) {
		parser.close();
	    }
	}
    }
}
