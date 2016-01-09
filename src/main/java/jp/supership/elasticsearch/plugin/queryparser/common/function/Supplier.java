/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.common.util.function;

import jp.supership.elasticsearch.plugin.queryparser.common.util.ObjectUtils;

/**
 * This interface specifies the implementing class has responsible of supplying result.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface Supplier<T> {
    /**
     * Gets a result.
     * @return a result
     */
    T get();
}
