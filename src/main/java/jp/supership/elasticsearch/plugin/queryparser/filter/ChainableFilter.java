/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.filter;

/**
 * This interface specifies the implementing class has ability to handle chained filter.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public abstract class ChainableFilter<T> implements Filter<T> {
    /** Holds the handling filtering context. */
    protected FilterContext<String, Object> context;

    /** Holds the next chainable instance. */
    protected ChainableFilter<T> next;

    /**
     * The entry point to the chaining process.
     * @throws FilterException if the filtering failed.
     */
    public T filter(T target) throws FilterException {
	target = this.doFilter(target, this.context);
	if (this.getNext() != null) {
	    this.getNext().setFilterContext(this.context);
	    target = this.getNext().filter(target);
	}
	return target;
    }

    /**
     * Returns the successing chainable instance.
     * @return the very next chainable instance.
     */
    public ChainableFilter<T> getNext() {
	return this.next;
    }

    /**
     * Sets the successing chainable instance.
     * @param next the very next chainable instance to be set.
     */
    public void setNext(ChainableFilter<T> next) {
	this.next = next;
    }

    /**
     * Returns the currently handling filter context.
     * @return the currently handling filter context.
     */
    public FilterContext<String, Object> getFilterContext() {
	return this.context;
    }

    /**
     * Sets the currently handling filter context.
     * @param context the filter cntext to be set.
     */
    public void setFilterContext(FilterContext<String, Object> context) {
	this.context = context;
    }
}
