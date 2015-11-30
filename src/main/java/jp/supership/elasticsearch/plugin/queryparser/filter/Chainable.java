/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.filter;

/**
 * This interface specifies the implementing class has ability to handle chained process.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public abstract class Chainable<T> {
    /** Holds the handling chaining context. */
    protected ChainContext<String, Object> chainContext;

    /** Holds the next chainable instance. */
    protected Chainable<T> next;

    /**
     * The entry point to the chaining process.
     * @throws FilterException if the filtering failed.
     */
    public void chain(T target) throws FilterException {
	this.doChain(target, this.chainContext);
	if (this.getNext() != null) {
	    this.getNext().setChainContext(this.chainContext);
	    this.getNext().chain(target);
	}
    }

    /**
     * Returns the successing chainable instance.
     * @return the very next chainable instance.
     */
    public Chainable<T> getNext() {
	return this.next;
    }

    /**
     * Sets the successing chainable instance.
     * @param next the very next chainable instance to be set.
     */
    public void setNext(Chainable<T> next) {
	this.next = next;
    }

    /**
     * Returns the currently handling chain context.
     * @return the currently handling chain context.
     */
    public ChainContext<String, Object> getChainContext() {
	return this.chainContext;
    }

    /**
     * Sets the currently handling chain context.
     * @param chainContext the currently handling chain cntext to be set.
     */
    public void setChainContext(ChainContext<String, Object> chainContext) {
	this.chainContext = chainContext;
    }

    /**
     * Returns {@code FieldQuery} in accordance to the assigned configuration.
     * @param  chained the instance to be handled with the successing chainable instances..
     * @param  chainContext the context to be shared withing the relating chains.
     * @throws FilterException if the filtering fails.
     */
    abstract protected void doChain(T chained, ChainContext<String, Object> chainContext) throws FilterException;
}
