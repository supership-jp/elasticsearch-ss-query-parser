/*
 * Copyright (C) 2015- Supership Inc.
 */
package jp.supership.elasticsearch.plugin.queryparser.antlr.v4.util;

import java.io.Reader;
import org.apache.lucene.search.Query;

/**
 * This interface specifies the implementing class can handle the given {@code java.io.Reader}
 * as raw query string and instanciates {@code org.apache.lucene.search.Query}.
 *
 * @author Shingo OKAWA
 * @since  1.0
 */
public interface DispatcherHandler extends QueryHandler {
    /**
     * Represents query handling context, i.e., in accordance to this instance's state, appropriate
     * {@code Query} will be instanciated.
     */
    public class Context {
	/** Holds currently handling field name. */
	private String field = null;
	/** Holds currently handling term token. */
	private String term = null;
	/** Holds currently handling fuzzy slop term token. */
	private String fuzzySlop = null;
	/** true if the process must be done as handling prefix query. */
        private boolean preferPrefix = false;
	/** true if the process must be done as handling wildcard query. */
	private boolean preferWildcard = false;
	/** true if the process must be done as handling fuzzy query. */
	private boolean preferFuzzy = false;
	/** true if the process must be done as handling regexp query. */
	private boolean preferRegexp = false;

	/** Returns the currently handling field. */
	public String getField() {
	    return this.field;
	}

	/** Sets the currently handling field. */
	public void setField(String field) {
	    this.field = field;
	}

	/** Return the currently handling term. */
	public String getTerm() {
	    return this.term;
	}

	/** Sets the currently handling term. */
	public void setTerm(String term) {
	    this.term = term;
	}

	/** Returns the assigned fuzzy-slop. */
	public String getFuzzySlop() {
	    return this.fuzzySlop;
	}

	/** Sets the assigned fuzzy-slop. */
	public void setFuzzySlop(String fuzzySlop) {
	    this.fuzzySlop = fuzzySlop;
	}

	/** Returns true when the {@code PrefixQuery} must handle the currently assigned query. */
	public boolean preferPrefix() {
	    return this.preferPrefix;
	}

	/** Sets the assigned prefix-query setting. */
	public void preferPrefix(boolean preferPrefix) {
	    this.preferPrefix = preferPrefix;
	}

	/** Returns true when the {@code WildcardQuery} must handle the currently assigned query. */
	public boolean preferWildcard() {
	    return this.preferWildcard;
	}

	/** Sets the assigned wildcard-querry setting. */
	public void preferWildcard(boolean preferWildcard) {
	    this.preferWildcard = preferWildcard;
	}

	/** Returns true when the {@code FuzzyQuery} must handle the currently assigned query. */
	public boolean preferFuzzy() {
	    return this.preferFuzzy;
	}

	/** Sets the assigned fuzzy-querry setting. */
	public void preferFuzzy(boolean preferFuzzy) {
	    this.preferFuzzy = preferFuzzy;
	}

	/** Returns true when the {@code RegexpQuery} must handle the currently assigned query. */
	public boolean preferRegexp() {
	    return this.preferRegexp;
	}

	/** Sets the assigned regexp-querry setting. */
	public void preferRegexp(boolean preferRegexp) {
	    this.preferRegexp = preferRegexp;
	}
    }

    /**
     * Dispatches appropriate query-builder in accordance to the given context.
     * @param  context the currently handling context.
     * @throws HandleException if the handling fails.
     */
    public void dispatch(DispatcherHandler.Context context);

    /**
     * Dispatches appropriate query-builder in accordance to the given context.
     * @param  context the currently handling context.
     * @throws HandleException if the handling fails.
     */
    public Query dispatchBareToken(DispatcherHandler.Context context) throws HandleException;

    /**
     * Dispatches appropriate query-builder in accordance to the given context.
     * @param  context the currently handling context.
     * @throws HandleException if the handling fails.
     */
    public Query dispatchQuotedToken(DispatcherHandler.Context context) throws HandleException;
}
